Fabwork
========

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
```
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
```
{
  "requiredModIds": [
	  "fabric", ...
	]
}
```

## Networking Abstraction

For more advanced betworking tools, look into the included SimpleNetworking class.
To register packets, call either SimpleNetworking.clientToServer or SimpleNetworking.serverToClient and store the returned type statically
like you would a block or item.

```
class ExampleMod {
  // registration
  C2SPacketType<ExamplePacket> EXAMPLE = SimpleNetworking.clientToServer(new Identifier("modid", "example"), ExamplePacket::new);

	static void example() {
	  // usage (on client)
		EXAMPLE.sendToServer(new ExamplePacket(1));
	}
}

class ExamplePacket extends Packet<ServerPlayerEntity> {

  private int parameter;

  ExamplePacket(int parameter) {
	  this.parameter = parameter;
	}
	
	ExamplePacket(PacketByteBuf buffer) {
	  this(buffer.readInt());
	}
	
	public void toBuffer(PacketByteBuf buffer) {
	  buffer.writeInt(parameter);
	}

  @Override
  public void handle(ServerPlayerEntity sender) {
	  // callback executed when receiving your packet
	}
}
```

## Maven:

Since this library is still WIP and undergoing testing, there won't be a build available on the maven _yet_.
Until that time, you can download a premade build from the releases tab on this github, create and publish your own
for local use.

1. `git clone https://github.com/Sollace/Fabwork.git`
2. `cd Fabwork`
3. `gradlew build publishToLocalMaven`
3. Add `mavenLocal()` to your build.gradle's repositories block
4. Add to your build.gradle's dependencies block:
```
    modApi "com.sollace:fabwork:${project.fabwork_version}"
    include "com.sollace:fabwork:${project.fabwork_version}"
```


Maven: `https://repo.minelittlepony-mod.com/maven/snapshot`

Dependency: `com.sollace:fabwork:1.1.0`
