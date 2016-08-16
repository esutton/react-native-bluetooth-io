/**
* Sample React Native App
* https://github.com/facebook/react-native
* @flow
*/

import React, { Component } from 'react';


import {
  Alert,
  AppRegistry,
  ListView,
  StyleSheet,
  Switch,
  Text,
  TouchableHighlight,
  View
} from 'react-native';

import BluetoothIO from 'react-native-bluetooth-io';
import Icon from 'react-native-vector-icons/MaterialIcons';

import {createTrackerConfiguration} from './TrackerConfiguration';

var base64 = require('base-64');
var nmea = require('nmea')
var utf8 = require('utf8');

let Buffer = require('buffer').Buffer;

let options = {
  encoding: 'utf8'
};


// Declare this before React.createClass:
var ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});

// class BluetoothIOExample extends Component {
//   constructor(props) {
//     super(props);
//
//     this.state = {
//       bluetoothState: 0,
//       deviceList: [],
//       dataSource: ds.cloneWithRows([]),
//     };
//
//     //this.render = this.render.bind(this);
// }

var BluetoothIOExample = React.createClass({

  getInitialState() {
    console.log('BluetoothIOExample: constructor');

    var ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});

    // the action setUnitType is changing store state
    // however, the prop.state change is not being reflected back to here

    return {
      bufferRx: '',
      bluetoothState: 0x00000000,
      bluetoothStateName: '',
      bluetoothOn: false,
      bluetoothSwitchDisabled: false,
      connectionStateName: '',
      dataSource: ds.cloneWithRows([]),
      deviceList: [],    };
    },


    scanBluetooth() {
      console.log("BluetoothIOExample:scanBluetooth");

      BluetoothIO.getDeviceList("TK")
      .then((deviceList) => {
        console.log('BluetoothIO.getDeviceList: ', deviceList);
        this.setState({
          deviceList: deviceList,
          dataSource: ds.cloneWithRows(deviceList),
        });
      })
      .catch((err) => {
        console.log(err.message);
      });

      // Check if Bluetooth is ON
      BluetoothIO.getState()
      .then((bluetoothState) => {
        console.log('this.statex= ', this.state);
        console.log('b4 BluetoothIO.getState: 0x' + bluetoothState.toString(16));

        this.setState({
          bluetoothState: bluetoothState,
          bluetoothOn: 0x0c === bluetoothState,
        });


      })
      .catch((err) => {
        console.log(err.message);
      });


    },

    componentDidMount() {
      BluetoothIO.componentDidMount();
      BluetoothIO.listenerAdd("onDataRx", this.onDataRx);
      BluetoothIO.listenerAdd("onStateChange", this.onStateChange);
      BluetoothIO.listenerAdd("onBluetoothStateChange", this.onBluetoothStateChange);
      this.onScan();

      this.testNmea();
    },

    testNmea() {
      var s = [
        "$GPGSA,A,1,,,,,,,,,,,,,,,*1E",
        "$GPGSV,3,1,12,29,75,266,39,05,48,047,,26,43,108,,15,35,157,*78",
        "$GPGSV,3,2,12,21,30,292,,18,21,234,,02,18,093,,25,13,215,*7F",
        "$GPGSV,3,3,12,30,11,308,,16,,333,,12,,191,,07,-4,033,*62",
        "$GPRMC,085542.023,V,,,,,,,041211,,,N*45",
        "$GPGGA,085543.023,,,,,0,00,,,M,0.0,M,,0000*58",
        "$IIBWC,160947,6008.160,N,02454.290,E,162.4,T,154.3,M,001.050,N,DEST*1C",
        "$IIAPB,A,A,0.001,L,N,V,V,154.3,M,DEST,154.3,M,154.2,M*19",
        "$IIHDM,201.5,M*24",
        "$PRDID,-4.44,2.12,154.25*56"
      ];

      console.log('*** Test GPS NMEA ***');
      for (var i=0; i < s.length; i++) {
        console.log('*** NMEA s[' + i + '] : {' + s[i] + '}');
        console.log(nmea.parse(s[i]));
      };
    },

    // Hex dump to console
    bufferLog(buffer, offsetStart, length) {
      console.log('bufferLog [' + length + '], offsetStart: ' + offsetStart);
      //console.log('typeof:', typeof buffer);

      let isString = typeof buffer === 'string';

      let i = 0;
      let row = 0;

      console.log('----: -- -- -- -- -- -- -- --  -- -- -- -- -- -- -- --');
      while( length > i) {
        let rowData = "";
        let rowStartPos = i;
        for(let col = 0; col < 16; ++col ) {

          let spacer = " ";
          if( 7 === col ) {
            spacer = "  ";
          }

          let dataDisplay = "  ";
          if( length > i ) {
            if(isString) {
              //rowData = rowData + ("00" + buffer.charCodeAt(i).toString(16)).substr(-2) + spacer;
              dataDisplay = ("00" + buffer.charCodeAt(i).toString(16)).substr(-2);
            } else {
              //rowData = rowData + ("00" + buffer[i].toString(16)).substr(-2) + spacer;
              dataDisplay = ("00" + buffer[i].toString(16)).substr(-2);
            }
          }
          rowData = rowData + dataDisplay + spacer;

          ++i;
        }

        if(isString) {
          rowData = rowData + " : " + JSON.stringify(buffer.slice(rowStartPos, i));
        }

        console.log(("0000" + row.toString(16)).substr(-4) + ": " + rowData);
        row = row + 16;
      }
    },

    // $TSI,15,0,322644,3,0,1,TK_0003,00:07:80:46:87:cd\r,20160815T123109,20130524T131700,20131023T173556,2\r\n
    onDataRx(e: Event) {
      console.log('Event onDataRx:', e);

      // Let's see if can use JS String for RX buffer
      // console.log('*** Buffer[',  e.data.length, ']: base64 ***');
      // let bufferNew = Buffer.alloc(e.data.length, e.data, 'base64');
      // this.bufferLog(bufferNew, 0, bufferNew.length);

      let asciiContents = '';
      if (options.encoding === 'utf8') {
        asciiContents = base64.decode(e.data);
        console.log('ascii[' + asciiContents.length + ']={' + asciiContents + '}');
        this.bufferLog(asciiContents, 0, asciiContents.length);
      }

      // Append new data to bufferRx
      let bufferRx = this.state.bufferRx + asciiContents;

      // Note: setState is an async function
      this.setState({
        bufferRx: bufferRx,
      },
      function() {
        // If here, then new state is in effect
        this.bufferLog(this.state.bufferRx, 0, this.state.bufferRx.length);

        let pos = this.state.bufferRx.indexOf('\r\n');
        if( 0 <= pos ) {
          pos = pos + 2;
          let foundcommand = this.state.bufferRx.slice(0, pos);

          console.log('*** Found command <CR><LF> { ' + JSON.stringify(foundcommand) + ' }');

          let lengthBufferBefore = this.state.bufferRx.length;
          let lengthBufferAfter = -1;
          let lengthCommand = foundcommand.length;

          if(foundcommand.startsWith("$TSI")) {
            let trackerConfiguration = createTrackerConfiguration(foundcommand);
            console.log('trackerConfiguration:', trackerConfiguration);
          } else if(foundcommand.startsWith("$")) {
            console.log('NMEA : { ' + JSON.stringify(foundcommand) + ' }');
            console.log(nmea.parse(foundcommand));
          }


          // Remove command from bufferRx
          // by keeping the data *after* the <CR><LF>
          let bufferRx = '';
          if(pos < this.state.bufferRx.length ) {
            bufferRx = this.state.bufferRx.slice(this.state.bufferRx, pos);
          }
          this.setState({
            bufferRx: bufferRx
          },
          function() {
            // lengthBufferAfter = this.state.bufferRx.length;
            // console.log('*** Buffer after remove:{' + foundcommand + '}');
            // console.log('    lengthBufferBefore.:', lengthBufferBefore);
            // console.log('    lengthCommand......:', lengthCommand);
            // console.log('    lengthBufferAfter..:', lengthBufferAfter);
            // console.log('    Expected..........:',
            // (lengthBufferBefore - lengthCommand),
            // lengthCommand === lengthBufferAfter);
            // this.bufferLog(this.state.bufferRx, 0, this.state.bufferRx.length);
          });

        }

      });

    },

    onStateChange(e: Event) {
      console.log('Event onStateChange:', e);
      this.setState({
        connectionStateName: e.name,
      });
    },
    onBluetoothStateChange(e: Event) {
      console.log('Event onBluetoothStateChange:', e);
      this.setState({
        bluetoothStateName: e.name,
      });

      let bluetoothSwitchDisabled = false;
      if('on' === e.name) {
        this.setState({
          bluetoothOn: true,
          bluetoothSwitchDisabled: bluetoothSwitchDisabled,
        });
        this.scanBluetooth();
      }
      if('off' === e.name) {
        this.setState({
          bluetoothOn: false,
          bluetoothSwitchDisabled: bluetoothSwitchDisabled,
        });
      }
      if('offTransition' === e.name ||
      'onTransition' === e.name) {
        bluetoothSwitchDisabled = true;
        this.setState({
          bluetoothSwitchDisabled: false,
        });
      }

    },

    onScan() {
      this.scanBluetooth();
    },

    onSend() {
      BluetoothIO.writeString("GETINFO\r\n")
    },

    renderHeader() {
      return (
        <View>
        <Text>Paired Devices</Text>
        </View>
      );
    },

    renderSectionHeader() {
      return (
        <View>
        <Text>Other Available Devices</Text>
        </View>
      );
    },

    renderSeparator(
      sectionID: number | string,
      rowID: number | string,
      adjacentRowHighlighted: boolean
    ) {
      var style = styles.rowSeparator;
      if (adjacentRowHighlighted) {
        style = [style, styles.rowSeparatorHide];
      }
      return (
        <View key={'SEP_' + sectionID + '_' + rowID}  style={style}/>
      );
    },

    onRowPress(device: Object) {
      let secure = false;
      let msg = 'Connect to: ' + device.name + ', ' + device.address;
      Alert.alert(
        msg,
        msg,
        [
          {text: 'Cancel', onPress: () => console.log('Cancel Pressed'), style: 'cancel'},
          {text: 'OK', onPress: () => BluetoothIO.connect(device, secure)},
        ]
      )

    },

    onBluetoothSwitchChange(value) {
      console.log('onBluetoothOnChange:', value);
      BluetoothIO.setBluetoothEnable(value);
    },

    renderRow(device: Object) {
      var chevronIcon = <Icon name="chevron-right" size={20} ></Icon>;

      return (
        <View style={styles.row}>

        <TouchableHighlight style={styles.row}
        onPress={() => this.onRowPress(device) }
        onShowUnderlay={this.props.onHighlight}
        onHideUnderlay={this.props.onUnhighlight}>

        <View style={styles.row}>
        <Text style={styles.welcome}>{device.name + ', ' + device.address}</Text>
        {chevronIcon}
        </View>

        </TouchableHighlight>

        </View >
      );
    },

    render() {
      console.log("BluetoothIOExample:render");

      let bluetoothStateString = '0x' + this.state.bluetoothState.toString(16);
      //console.log('this.state= ', this.state);

      return (
        <View style={styles.container}>

        <Text style={styles.welcome}>
        ........
        </Text>

        <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
        <Text>Bluetooth {this.state.bluetoothOn ? 'On' : 'Off'} ({this.state.bluetoothStateName})</Text>
        <View>
        <Switch
        onValueChange={(value) => this.onBluetoothSwitchChange(value)}
        style={{marginBottom: 10}}
        value={this.state.bluetoothOn}
        disabled={this.state.bluetoothSwitchDisabled} />
        </View>
        </View>


        <TouchableHighlight style={styles.row}
        onPress={this.onScan} >
        <View style={styles.row} >

        <Icon name='bluetooth-searching' size={20}  color='blue'>
        </Icon>
        <Text style={styles.welcome}>
        Bluetooth Scan
        </Text>
        </View>
        </TouchableHighlight>

        <TouchableHighlight style={styles.row}
        onPress={this.onSend} >
        <View style={styles.row} >

        <Icon name='send' size={20}  color='black'>
        </Icon>
        <Text style={styles.welcome}>
        Send ({this.state.connectionStateName})
        </Text>
        </View>
        </TouchableHighlight>

        <Text style={styles.welcome}>
        Bluetooth State: {bluetoothStateString}
        </Text>
        <Text style={styles.instructions}>
        Device List
        </Text>

        <View style={styles.listContainer}>
        <ListView
        ref="listview"
        style={styles.list}
        renderSeparator={this.renderSeparator}
        dataSource={this.state.dataSource}
        renderHeader={this.renderHeader}
        renderSectionHeader={this.renderSectionHeader}
        renderRow={this.renderRow}
        //renderFooter={this.renderFooter}
        automaticallyAdjustContentInsets={false}
        keyboardDismissMode="on-drag"
        keyboardShouldPersistTaps={true}
        showsVerticalScrollIndicator={false}
        enableEmptySections
        />
        </View>

        </View>
      );
    },

  });

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#F5FCFF',
    },
    welcome: {
      fontSize: 20,
      textAlign: 'center',
      margin: 10,
    },
    instructions: {
      textAlign: 'center',
      color: '#333333',
      marginBottom: 5,
    },
    listContainer: {
      flex: 1,
    },
    list: {
      marginTop: 4,
      backgroundColor: '#eeeeee',
    },
    row: {
      alignItems: 'center',
      flexDirection: 'row',
      justifyContent: 'space-between',
    },
  });

  export default BluetoothIOExample;
