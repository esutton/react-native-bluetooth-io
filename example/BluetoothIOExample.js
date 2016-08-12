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

var base64 = require('base-64');
var utf8 = require('utf8');


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
    },

    onDataRx(e: Event) {
      console.log('Event onDataRx:', e);

      // if (options.encoding === 'utf8') {
      //   contents = utf8.decode(base64.decode(b64));
      // } else if (options.encoding === 'ascii') {
      //   contents = base64.decode(b64);
      // } else if (options.encoding === 'base64') {
      //   contents = b64;
      // } else {
      //   throw new Error('Invalid encoding type "' + String(options.encoding) + '"');
      // }
      let contents = utf8.decode(base64.decode(e.data));
      console.log('decodeUtf8:', contents);

      contents = base64.decode(e.data);
      console.log('ascii:', contents);


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
      Alert.alert('onRowPress ' + device.name + ', ' + device.address);

      let secure = false;
      BluetoothIO.connect(device, secure);
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
      console.log('this.state= ', this.state);

      return (
        <View style={styles.container}>

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
