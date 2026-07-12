#!/bin/bash

# Configuration
USER_POOL_ID="us-east-1_h0uGcZU1T"
REGION="us-east-1"

echo "=================================================="
echo "=== 1. AWS COGNITO USER POOL DETAILS ==="
echo "=================================================="
aws cognito-idp describe-user-pool \
  --user-pool-id "$USER_POOL_ID" \
  --region "$REGION" \
  --query "UserPool.{Id:Id,Name:Name,Status:Status,UsernameAttributes:UsernameAttributes,RequiredAttributes:SchemaAttributes[?Required==\`true\`].Name}" \
  --output json

echo ""
echo "=================================================="
echo "=== 2. COGNITO USERS LIST ==="
echo "=================================================="
aws cognito-idp list-users \
  --user-pool-id "$USER_POOL_ID" \
  --region "$REGION" \
  --query "Users[*].{Username:Username,Enabled:Enabled,UserStatus:UserStatus,Email:Attributes[?Name==\`email\`].Value | [0], Phone:Attributes[?Name==\`phone_number\`].Value | [0]}" \
  --output json

echo ""
echo "=================================================="
echo "=== 3. DYNAMODB USER TABLES ==="
echo "=================================================="
TABLES=$(aws dynamodb list-tables --region "$REGION" --query "TableNames" --output text)
USER_TABLE=""

for table in $TABLES; do
    if [[ $table == *"User"* ]]; then
        USER_TABLE=$table
        echo "Found User Table: $table"
    fi
done

if [ -n "$USER_TABLE" ]; then
    echo ""
    echo "=================================================="
    echo "=== 4. DYNAMODB USER ITEMS (First 10) ==="
    echo "=================================================="
    aws dynamodb scan \
      --table-name "$USER_TABLE" \
      --region "$REGION" \
      --max-items 10 \
      --query "Items[*].{id:id.S, phone:phone.S, email:email.S, name:name.S}" \
      --output json
else
    echo "No DynamoDB User Table found."
fi
