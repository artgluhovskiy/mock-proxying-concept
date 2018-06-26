# Simple proxy based mock framework implementation

The code demonstrates a simple example of proxy based mock creation.
Simple mock tests include creation, arrangement, action and verification
stages. Verification options like method invocation ordering are not considered.
Two proxy techniques were implemented:
- based on java.lang.reflect.Proxy
- CGLib proxying
