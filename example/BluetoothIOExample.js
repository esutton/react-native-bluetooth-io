/**
* Sample React Native App
* https://github.com/facebook/react-native
* @flow
*/

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';

import BluetoothIO from 'react-native-bluetooth-io';

class BluetoothIOExample extends Component {
  render() {
    console.log("RNBluetoothIO:", BluetoothIO);
    //console.log(BluetoothIO.function1());

    // Get Local IP
    BluetoothIO.getIPAddress(ip => {
      console.log("BluetoothIO.getIPAddress:", ip);
    });

    return (
      <View style={styles.container}>
      <Text style={styles.welcome}>
      Welcome to BluetoothIOExample!
      </Text>
      <Text style={styles.instructions}>
      sayHello: {BluetoothIO.getGreeting()}
      </Text>
      <Text style={styles.instructions}>
      Double tap R on your keyboard to reload,{'\n'}
      Shake or press menu button for dev menu
      </Text>
      </View>
    );
  }
}

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
});

export default BluetoothIOExample;
