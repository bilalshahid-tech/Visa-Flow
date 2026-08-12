import os

poms = [
    r"c:\Users\Administrator\Desktop\visa flow\case-service\pom.xml",
    r"c:\Users\Administrator\Desktop\visa flow\document-service\pom.xml",
    r"c:\Users\Administrator\Desktop\visa flow\notification-service\pom.xml",
    r"c:\Users\Administrator\Desktop\visa flow\risk-service\pom.xml",
    r"c:\Users\Administrator\Desktop\visa flow\audit-service\pom.xml"
]

yamls = [
    r"c:\Users\Administrator\Desktop\visa flow\auth_service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\user_service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\case-service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\document-service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\notification-service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\risk-service\src\main\resources\application.yml",
    r"c:\Users\Administrator\Desktop\visa flow\audit-service\src\main\resources\application.yml"
]

for pom in poms:
    with open(pom, 'r') as f:
        content = f.read()
    if 'spring-boot-starter-actuator' not in content:
        content = content.replace('<dependencies>', '<dependencies>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-actuator</artifactId>\n        </dependency>')
        with open(pom, 'w') as f:
            f.write(content)

health_block = """
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
"""

for yml in yamls:
    with open(yml, 'r') as f:
        content = f.read()
    if 'management:' not in content:
        with open(yml, 'a') as f:
            f.write('\n' + health_block)
