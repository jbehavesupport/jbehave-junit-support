Scenario: login to system 2
Given login with data:
|username  |password   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
|JohnDow   |Passw0rd   |
When I submit login data on http://test02
Then user should be logged in successful
