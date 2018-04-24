# Black Swan Gargoyle

Black Swan Gargoyle (for Android) is a SDK for collecting telemetry and dimensional data from the mobile phone, sending them to our servers for processing and analysis (optional) and showing them as events in the application's text logs.

## Enabling sending to the server

To enable the optional function of sending the collected data to our servers for further analysis, please put the following code inside the `<application></<application>` tag in your application manifest XML file:

```
<meta-data
	android:name="bsgargoyle.send_data"
	android:value="true" />
```

The value can be set to `true` or `false` depending on whether you want to enable this function or not.
