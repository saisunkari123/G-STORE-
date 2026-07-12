export const handler = async (event) => {
  // Instantly confirm the user's registration
  event.response.autoConfirmUser = true;

  // Mark phone and email as already verified to prevent SMS/Email OTPs during sign-up
  if (event.request.userAttributes.hasOwnProperty("email")) {
      event.response.autoVerifyEmail = true;
  }
  if (event.request.userAttributes.hasOwnProperty("phone_number")) {
      event.response.autoVerifyPhone = true;
  }

  return event;
};
