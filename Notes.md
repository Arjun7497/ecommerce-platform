**User Service**

User Entity Object

- id Generated value (type = UUID) which is the primary key for the User entity.
- email String representing the user's email address. This field is unique and cannot be null.
- password String representing the user's password. This field cannot be null and should be stored securely (e.g., hashed).
- firstName String representing the user's first name. This field cannot be null.
- lastName String representing the user's last name. This field cannot be null.
- Role is an enumeration representing the user's role in the system (e.g., ADMIN, CUSTOMER). This field cannot be null. by default, the role is set to CUSTOMER.
- active Boolean indicating whether the user's account is active. This field cannot be null and defaults to true.
- createdAt Timestamp representing the date and time when the user account was created. This field cannot be null and is automatically set when the user is created. 
we used builder.default to set the default value of createdAt to the current timestamp.
- updatedAt Timestamp representing the date and time when the user account was last updated. This field cannot be null and 
is automatically updated whenever the user entity is modified. We used builder.default to set the default value of updatedAt to the current timestamp.
