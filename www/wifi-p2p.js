var service = ""
module.exports = {
    startScan: function (success, error) {
        cordova.exec(success, error, service, "ACTION_START_SCAN")
    },

    connectToPeer: function (success, error, address) {
        cordova.exec(success, error, service, "ACTION_CONNECT_TO_PEER", {address: address})
    },

    cancelConnect: function () {
        cordova.exec(success, error, service, "ACTION_CANCEL_CONNECT")
    },

    stopDiscovery: function () {
        cordova.exec(null, null, service, "ACTION_STOP_DISCOVER_PEER")
    }
}