Fabwork
========

[![Current Version](https://img.shields.io/github/v/release/MineLittlePony/Fabwork)](https://github.com/MineLittlePony/Fabwork/releases/latest)
[![Build Status](https://github.com/Sollace/Fabwork/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/Sollace/Fabwork/actions/workflows/gradle-build.yml)
![License](https://img.shields.io/github/license/Sollace/Fabwork)
![Platform](https://img.shields.io/badge/api-fabric-orange.svg)

Fabwork is a networking library and extension to FabricAPI that provides two main purposes:

1. It performs validation to ensure neither side is missing mods required by the other.
2. It provides a wrapper around Fabric's own networking apis that extends them with a more high-level interface and adds additional functions not normally available.


## Modders: How to Use

If all you need from fabwork is the validation aspect, then there are no code changes required.
Any mods that include the optional "fabwork" custom attribute in their fabric.mod.json will be considered when joining a server.

fabric.mod.json
```json
{
  "custom": {
      "fabwork": {
        "requiredOn": "<*|client|server>"
      }
  }
}
```


## Server Operators: How to Use

If you have Fabwork installed on the server, you can specify additional join requirements by editing the config generated at server_directory/config/fabwork.json

Mod ids added to the "requiredModIds" list will be automatically included when determining whether a client is able to connect.

fabwork.json
```json
{
  "requiredModIds": [
    "fabric-api", ...
  ]
}
```

## Networking Abstraction

For more advanced networking tools, look into the included SimpleNetworking class.
To register packets, call either `SimpleNetworking.clientToServer` or `SimpleNetworking.serverToClient` and store the returned type statically
like you would a block or item.

```java
class ExampleMod implements ModInitializer {
  // registration
  C2SPacketType<ExampleServerBoundPacket> EXAMPLE_SERVER_BOUND = SimpleNetworking.clientToServer(new Identifier("modid", "example_server_bound"), ExampleServerBoundPacket::new);
  S2CPacketType<ExampleClientBoundPacket> EXAMPLE_CLIENT_BOUND = SimpleNetworking.serverToClient(new Identifier("modid", "example_client_bound"), ExampleClientBoundPacket::new);
  @Override
  public void onInitialize() {
     EXAMPLE_SERVER_BOUND.receiver().addPersistentHandler((sender, packet) -> {
       // callback executed when receiving your packet
     });
     // send packet to client
     EXAMPLE.sendToPlayer(new ExampleClientBoundPacket(1), aServerPlayerEntity);
  }
}

class ExampleModClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
     EXAMPLE_CLIENT_BOUND.receiver().addPersistentListener(this::onExamplePacket);
  }

  private void onExamplePacket(Player sender, ExampleClientBoundPacket packet) {
    // do something
    // send packet to server
    EXAMPLE.sendToServer(new ExampleServerBoundPacket(packet.parameter));
  }
}

record ExampleServerBoundPacket (int parameter) {
  public static final StreamCodec<ExampleServerBoundPacket> STREAM_CODEC = StreamCodec.composite(
     ByteBufCodecs.INT, ExampleServerBoundPacket::parameter,
     ExampleServerBoundPacket::new
  );
}

record ExampleClientBoundPacket (int parameter) {
  public static final StreamCodec<ExampleClientBoundPacket> STREAM_CODEC = StreamCodec.composite(
     ByteBufCodecs.INT, ExampleClientBoundPacket::parameter,
     ExampleClientBoundPacket::new
  );
}
```

## Maven:

Maven: `https://repo.minelittlepony-mod.com/maven/releases`

Dependency: `com.sollace:fabwork:${project.fabwork_version}`
