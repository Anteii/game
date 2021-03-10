let connect, disconnect, sendName;
let stompClient = null;

import("./webSockLib.js").then(
    m => {
        connect = m.sendName;
        disconnect = m.disconnect;
        sendName = m.sendName;
    }
);

