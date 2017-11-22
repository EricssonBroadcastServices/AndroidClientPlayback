# Custom Playback Tech

Although unlikely, it is possible for a customer to develop its own tech for playback.
To do so, there are 2 necessary steps to take:

1. Make the tech specific implementation, not forgetting to implement **ITech** interface.
2. Extend class **TechFactory** and override method ``build()`` that should return a new instance of the new tech 
3. When calling one of the playback factories (``EMPPlayerFactory``, ``EntitlementPlayerFactory``, ``PlayerFactory``), pass a new instance of the newly created ``TechFactory`` extension.

```java
EntitlementPlayerFactory.build(entitlementProvider, analytics, myActivity, myView, new MyDearNewTechFactory());
```

- ``entitlementProvider`` is an instance of a class that implements **IEntitlementProvider** interface
- ``analyticsConnector`` is an instance of a class that extends **AnalyticsPlaybackConnector**;
- ``myActivity`` is the activity using the player
- ``myView`` is the reference to the player's wrapper View (must extend ViewGroup)
- ``MyDearNewTechFactory`` is a nre instance of your own tech factory extension
