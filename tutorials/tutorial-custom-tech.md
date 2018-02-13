# Custom Playback Tech

It is possible to develop your own tech for playback.
To do so, there are 2 necessary steps to take:

1. Make the tech specific implementation, not forgetting to implement **ITech** interface.
2. Extend the **TechFactory** class and override the ``build()`` method that should return a new instance of the new tech 
3. When calling one of the playback factories (``EMPPlayerFactory``, ``EntitlementPlayerFactory``, ``PlayerFactory``), pass a new instance of the newly created ``TechFactory`` extension.

```java
EntitlementPlayerFactory.build(entitlementProvider, analytics, myActivity, myView, new MyDearNewTechFactory());
```

- ``entitlementProvider`` is an instance of a class that implements **IEntitlementProvider** interface
- ``analyticsConnector`` is an instance of a class that extends **AnalyticsPlaybackConnector**;
- ``myActivity`` is the activity that uses the player
- ``myView`` is the reference to the players wrapper View (must extend ViewGroup)
- ``MyDearNewTechFactory`` is a new instance of your own tech factory extension
