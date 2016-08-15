/**
* @file TrackerConfiguration.js
* @copyright The Charles Machine Works, Inc., 2016
* @copyright All rights reserved http://www.ditchwitch.com/
*
* @description Parse Tracker configuration repsonse to GETINFO
*
* @Author Edward Sutton <edward.sutton@subsite.com>
*
* @flow
*/
'use strict';


// http://subsitedesign/wiki/index.php/TK_Series_commands#GETINFO
const fieldNameList = [
  "header",
  "version",
  "versionDsp",
  "onTimeMinutes",
  "serialNumber",
  "model",
  "radio",
  "bluetoothName",
  "bluetoothAddress",
  "dateTimeCurrent",
  "dateTimeManufacture",
  "dateTimeWarranty",
  "trackerControl",
];

// Production Example:
// $TSI,12,0,28228,3,0,1,TK_0003,00:07:80:46:87:cd,20150707T162616,20130524T131700,20131023T173556,2
//
// Engineering Example ( last two dates are in abnormal -1 field format, I assume because not inititialized ).
// "$TSI,99,0,58,9990004,0,1,TK_0004,00:07:80:36:26:3d\r,20150724T094509,-001-1-1T-1-1-1,-001-1-1T-1-1-1,1"
export function createTrackerConfiguration(getInfoResponse) {
  let fieldList = getInfoResponse.trim().split(',');
  let data = {};
  for(let i = 1; i < fieldList.length; ++i) {
    //console.log(i, fieldNameList[i], fieldList[i]);
    data[fieldNameList[i]] = fieldList[i].trim();
  }
  return data;
}

export default { createTrackerConfiguration };
