// Helpers
const displayView = name => ['loading', 'error', 'authenticated', 'unauthenticated'].forEach(v => document.getElementById(`view-${v}`).hidden = v !== name);
const setTextContent = (id, content) => document.getElementById(id).textContent = content;

(async () => {
  // Initialize the Auth0 SDK
  window.client = await auth0.createAuth0Client({
    domain: 'dev-spmml3rlp721xmv5.us.auth0.com',
    clientId: 'AWMPYAUZW1uukdMIXmGTyfOZIhUJWY69',
    authorizationParams: { redirect_uri: location.origin },
  });

  // Handle errors returned by Auth0 after a redirect
  if (location.search.includes("error=")) {
    const params = new URLSearchParams(location.search);
    setTextContent("view-error", `Error: ${params.get("error")} — ${params.get("error_description")}`);
    displayView("error");
    history.replaceState({}, "", location.pathname);
    return;
  }

  // Handle the redirect callback after a successful login
  if (location.search.includes("code=") && location.search.includes("state=")) {
    await window.client.handleRedirectCallback();
    history.replaceState({}, "", location.pathname);
  }

  if (await window.client.isAuthenticated()) {
    const user = await window.client.getUser();
    console.log(user);
    setTextContent("user-email", user.email);
    setTextContent("user-profile", JSON.stringify(user, null, 2));

    const accessToken = await client.getTokenSilently();
    setTextContent("user-jwt", accessToken);

    const claims = await client.getIdTokenClaims();
    setTextContent("user-claims", JSON.stringify(claims, null, 2));


    displayView("authenticated");
    return;
  }

  displayView("unauthenticated");
})();
