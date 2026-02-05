import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Please find inline code-review style comments on the provided code and the second method which proposes some
 * changes to the provided code.
 */
class Example {

    @PostMapping("shops/{shopId}/invite")
    public ResponseEntity<?> inviteUser(
            @PathVariable String shopId,
            @RequestBody InvitationRequest invitationRequest) {
        // This looks a little surprising - we POST to AUTH_URL, but we forward it the invitation request and expect
        // the CREATED status code. Is this really authentication? Is this creating a new user? It's a little hard to
        // tell with no comments and the mix of invitationRequest + AUTH_URL.
        //
        // Most of this logic should probably live in a Service class, not directly in the controller.
        //
        // I'm not sure what the request to AUTH_URL wants to achieve, but we naturally run the risk of it succeeding
        // but not returning to us (network error) or the JVM being killed after we get the response but don't
        // actually process it - potentially this should be handled by something event driven like Kafka to avoid
        // losses.
        // Unless it is actually authentication. Then presumably we should be OK with any 2xx response. What's the
        // authId then? We create a new user based on it, will it be the same all the time for a given email? Then
        // many users won't require creation (but then how is it authentication if the tokens are eternal?) and just
        // a lookup in the repository and being added to the right shop.
        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity(AUTH_URL, invitationRequest, AuthResponse.class);
        // Use early return to avoid indentation and cognitive load for the reader
        if (response.getStatusCode() == HttpStatus.CREATED) {
            User user = userRepository.save(
                    new User(response.getBody().getAuthId(), invitationRequest.getEmail()));
            Shop shop = shopRepository.findById(shopId).orElse(null);
            // There's a good chance we want to check this first and avoid POST request + creating the user if the shop
            // doesn't exist. Unless the POST is cheaper than repository lookup that is.
            if (shop == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Shop not found");
            }
            if (!shop.getInvitations().contains(response.getBody().getInvitationId())) {
                // getInvitations would ideally return an immutable collection, in which case this will throw. Otherwise
                // we should consider refactoring it to immutable.
                shop.getInvitations().add(response.getBody().getInvitationId());
            }
            // Should we add the invitationId above even if the user was already a member of this shop?
            //
            // Can the user even not be a member of this shop if they are created above?
            if (!shop.getUsers().contains(user)) {
                shop.getUsers().add(user);
            }
            // Save to the repository can fail for any number of reasons, what if we saved the user but not the
            // updated definition of shop? This should potentially happen within a transaction to avoid this.
            shopRepository.save(shop);
        }
        // We will return OK even if the AuthResponse is not a success, which we probably don't want.
        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("shops/{shopId}/invite")
    @Transactional
    public ResponseEntity<?> inviteUserReviewed(
            @PathVariable String shopId,
            @RequestBody InvitationRequest invitationRequest) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The shop with the provided ID does not exist");
        }

        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity(AUTH_URL, invitationRequest, AuthResponse.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            // Should probably be more robust code to decide between bad request and other possibilities, but for
            // simplicity let's just indicate that an error happened.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        User user = userRepository.save(
                new User(response.getBody().getAuthId(), invitationRequest.getEmail()));

        if (!shop.getInvitations().contains(response.getBody().getInvitationId())) {
            shop.getInvitations().add(response.getBody().getInvitationId());
        }
        if (!shop.getUsers().contains(user)) {
            shop.getUsers().add(user);
        }
        // When using hibernate, the save inside a transaction for an existing entity should not be necessary.
        shopRepository.save(shop);

        return ResponseEntity.ok(response.getBody());
    }

}