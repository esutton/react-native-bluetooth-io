/**
* Sample React Native App
* https://github.com/facebook/react-native
* @flow
*/

import React, { Component } from 'react';
import {
  AppRegistry,
  ListView,
  StyleSheet,
  Text,
  TouchableHighlight,
  View
} from 'react-native';

import BluetoothIO from 'react-native-bluetooth-io';

import Icon from 'react-native-vector-icons/MaterialIcons';

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
    //dataSource: ds.cloneWithRows([]),
    //deviceList: [],
    return {
      bluetoothState: 0x00000000,
    };
  },


  scanBluetooth() {
    // console.log("BluetoothIOExample:componentDidMount");
    // BluetoothIO.getDeviceList("TK")
    // .then((deviceList) => {
    //   console.log('BluetoothIO.getDeviceList: ', deviceList);
    //   this.setState = {
    //     deviceList: result,
    //     dataSource: ds.cloneWithRows(deviceList),
    //   };
    // })
    // .catch((err) => {
    //   console.log(err.message);
    // });

    // Check if Bluetooth is ON
    BluetoothIO.getState()
    .then((bluetoothState) => {
      console.log('this.statex= ', this.state);
      console.log('b4 BluetoothIO.getState: 0x' + bluetoothState.toString(16));

      this.setState({
        bluetoothState: bluetoothState,
      });


    })
    .catch((err) => {
      console.log(err.message);
    });


  },

  componentDidMount() {
    this.onScan();
  },

  onScan() {
    this.scanBluetooth();
  },

  render() {
    console.log("BluetoothIOExample:render");

    // BluetoothIO.getDeviceList returns:
    // [
    // { name: 'TK_0003', address: '00:07:80:46:87:CD' },
    // { name: 'TK_0100', address: '00:07:80:A1:17:69' },
    // { name: 'BlueStar GNSS 18300008', address: '00:07:80:0D:21:0E' }
    // ]
    // BluetoothIO.getDeviceList("TK")
    // .then((deviceList) => {
    //    console.log('BluetoothIO.getDeviceList: ', deviceList);
    //    this.state = {
    //      deviceList: result,
    //      dataSource: ds.cloneWithRows(deviceList),
    //    };
    // })
    // .catch((err) => {
    //   console.log(err.message);
    // });

    let bluetoothStateString = '0x' + this.state.bluetoothState.toString(16);
    console.log('this.state= ', this.state);

    return (
      <View style={styles.container}>

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

      <Text style={styles.welcome}>
      Bluetooth State: {bluetoothStateString}
      </Text>
      <Text style={styles.instructions}>
      sayHello: {BluetoothIO.getGreeting()}
      </Text>
      <Text style={styles.instructions}>
      XYZZZ
      </Text>
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
  row: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
});

export default BluetoothIOExample;
