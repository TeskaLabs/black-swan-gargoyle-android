# BSMTTelemetry SDK

## Events

There are four events so far that the service sends to the BS server and to the application via internal messaging system so it can show logs.

When there is some important change to the variables, the events are always resent. The Basic event is also resent when the GPS position changes.

The format of events is JSON in all cases.

### General information

All events hold the following information:

**@timestamp** *long*

**L**: { "lat": *double*, "lon": *double* }

**event_type** *int* (see headers below)

**vendor_model** *string*

**phone_type** *string*

**IMSI** *string*

**IMEI** *string*

**MSISDN** *string*

**iccid** *string*

### Basic event (event_type = 0)

This event contains no additional information and is sent when some of the basic information stated above change, especially the GPS position.

### Connection event (event_type = 1)

This event contains additional information related to the internet connection, especially related to the fact whether the user is using the phone data connection.

**have_mobile_conn** *boolean*

**dconn** *int*

**roaming** *int*

### Phone event (event_type = 2)

This events contains information about the phone data states and call states.

**data_state** *int*

**data_network_type** *int*

**sig_ASU** *int*

**sig_dbm** *int*

**data_activity_dir** *int*

**call_state** *int*

**RX** *long*

**TX** *long*

**Clg** *string*

### Cell event (event_type = 3)

This event contains information about the cell identity and location. Only a few of the following variables are present at a device.

**ASU** *int*

**BSID** *int*

**BSILat** *int*

**BSILon** *int*

**ci** *int*

**cid** *int*

**dbm** *int*

**enb** *int*

**lac** *int*

**NetID** *int*

**pci** *int*

**psc** *int*

**rnc** *int*

**SysID** *int*

**tac** *int*

**TimAdv** *int*

### Attention

For lookup formatters for some integer values see the class *BSMTTEvents*.