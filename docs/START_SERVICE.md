# BSGargoyle Service

## Starting the service from another app

The service can be started from another application via Intents. To do so, place the following lines to your code. The variable `context` represents the application context or activity you would like to start the service from.

```
Intent broadcastIntent = new Intent("com.teskalabs.blackswan.gargoyle.BSWakefulGargoyleService");
context.sendBroadcast(broadcastIntent);
```

This code actually calls the BSWakefulReceiver to start the service with a wakelock that ensures the CPU runs. The service then runs independently of your application and should stay in the `Sending` state.

## Stopping the service from another app

The service can then be stopped using the following code in your application. The variable `context` represents the application context or activity you would like to stop the service from, if possible.

```
Intent intent = new Intent(context, BSGargoyleService.class);
context.stopService(intent);
```

The service should then be in the `Stopped` or `Stopped with process on` state.

## Monitoring the service

You can use `BSServiceMonitor` class from BSGargoyle SDK to monitor the service's states in your application. The class is located in the `monitor` package. 

To create the monitor, place the following code in the `onCreate` method of your application, where `mMonitor` is a property variable of your activity class.

```
mMonitor = new BSServiceMonitor(this, this);
mMonitor.updateWithTimeOrEvent();	
```

Your activity then should implement `BSServiceMonitorListener` interface and the method `void onReceiveServiceState(int state)`.

```
@Override
public void onReceiveServiceState(int state) { });	
```

The monitor properly works only when the following conditions are satisfied:

1.) The periodic timer is started, i. e. `mMonitor.startTimer(PERIOD_MS);`*

2.) The monitor can listen to service's messages. Since you work outside of the service's context, you will **not** be able to indicate the `STATE_SENDING`. You should implement your own check of the state based upon your own conditions.

*Do not forget to stop it when your application finishes, with `mMonitor.stopTimer()`.

For reference implementation, please see the demo app's code.

## Using the SDK directly

If you use BSGargoyle SDK directly as part of your own application, you also need to explicitly enable the data sending to our servers.

### Enabling sending to the server

To enable the optional function of sending the collected data to our servers for further analysis, please put the following code inside the `<application></<application>` tag in your application manifest XML file:

```
<meta-data
	android:name="com.teskalabs.blackswan.gargoyle.use_seacat"
	android:value="true" />
```

The value can be set to `true` or `false` depending on whether you want to enable this function or not.

