# G-Store Authentication & Registration Workflow

This document explains the internal mechanisms of the Registration, Login, and Forgot Password flows. This acts as a reference for any future debugging or modifications.

## Core Setup
- **Identity Provider**: AWS Cognito (`us-east-1`).
- **Database**: AWS AppSync (DynamoDB).
- **Username Configuration**: The Cognito User Pool is configured to strictly use the **Phone Number** as the unique `Username`. The email is simply saved as an extra attribute. You cannot register two accounts with the exact same phone number.

## 1. Customer Registration Flow
1. **User Input**: The user enters their Name, Phone, Email, and Password.
2. **Raw AWS API Call**: We bypass the standard `Amplify.Auth.signUp` function (which sometimes mangles attributes) and instead make a raw HTTP POST request to the Cognito REST API (`AWSCognitoIdentityProviderService.SignUp`).
3. **AWS Lambda Auto-Confirm**: Before creating the user, Cognito triggers our custom AWS Lambda function (`PreSignUpAutoConfirm`). 
   - This Lambda automatically marks the user as `CONFIRMED`.
   - It also marks the email and phone as verified.
   - This prevents Cognito from sending an OTP code to the user during registration.
4. **Auto-Login**: Because the user is instantly confirmed by the Lambda, the app successfully calls `Amplify.Auth.signIn(phone, password)`.
5. **Database Sync**: The app takes the newly created Cognito user and saves their profile (Name, Phone, Email, Role) to the AWS AppSync DynamoDB database via `userRepository.saveUser`.

## 2. Customer Login Flow
1. **Authentication**: The user enters their phone number and password. The app calls `Amplify.Auth.signIn`.
2. **Profile Fetch**: If successful, the app asks DynamoDB for the user's full profile to ensure they are actually a `CUSTOMER`.
3. **Self-Healing Fallback**: If a user successfully registered in Cognito, but the app crashed before saving them to DynamoDB, the login flow catches this! It fetches their Name/Email from the Cognito session attributes, reconstructs their profile, and saves it to DynamoDB in the background automatically.

## 3. Admin Login Flow
1. **Email Mapping**: Admins log in using an email (e.g., `admin@gstore.com`). However, because our Cognito User Pool *only* accepts phone numbers for login, the app intercepts the login attempt.
2. **Translation**: It instantly translates `admin@gstore.com` to a secure hardcoded phone number (`+910000000001`), and `developer@gstore.com` to `+910000000002`. 
3. **Login**: It then calls `Amplify.Auth.signIn` using the mapped phone number.

## 4. Forgot Password Flow
1. **Initiation**: The user types their phone number. The app calls `Amplify.Auth.resetPassword` directly. 
2. **Cognito Validation**: We rely entirely on AWS Cognito to validate the user. If the phone number exists, Cognito emails a 6-digit verification OTP to the user's registered email address. If the phone number does not exist, Cognito natively throws a `UserNotFoundException` which the app catches and displays as "User not found".
3. **Reset**: The user enters the OTP code from their email along with their new password. The app calls `Amplify.Auth.confirmResetPassword` to finish the reset.
