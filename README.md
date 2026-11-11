\# Online Status Checker



A Java TCP socket project that tracks whether my phone is online. The phone sends heartbeats to a server on my laptop, the server logs every status change to SQLite, and a live web page shows the phone's status.



\## How it works

\- `HeartbeatClient` (runs on the phone via Termux) sends a heartbeat every 2 seconds over TCP port 5000. It retries if the connection drops.

\- `StatusServer` marks the phone \*\*online\*\* on connect and \*\*offline\*\* if the connection closes or no heartbeat arrives for 5 seconds.

\- Each status change is stored in a SQLite table `status\_log`.

\- A built-in web server on port 8080 serves `server/index.html`, which checks the status every 2 seconds.



\## The web page

\- \*\*Online:\*\* green screen, the phone straightens to face you, the status dot beats like a heartbeat, and the background map zooms in from a wide overview to wide roads and large building blocks.

\- \*\*Offline:\*\* red screen, the phone tilts back, the dot stops, and the map zooms back out.

\- \*\*Server down:\*\* grey screen.



The map is purely decorative. It is an abstract drawing, and the server never knows the phone's location.



\## Project structure

```

status-checker/

├── client/HeartbeatClient.java

├── server/

│   ├── StatusServer.java

│   ├── ViewLog.java

│   ├── index.html

│   └── lib/        (put the sqlite-jdbc jar here)

├── docs/

└── README.md

```



\## Requirements

\- Java 17

\- The \[sqlite-jdbc](https://github.com/xerial/sqlite-jdbc/releases) jar (the plain `sqlite-jdbc-<version>.jar`), placed in `server/lib/`



\## Run

Server (from inside `server/`, so it can find `index.html`; Windows):

```

javac StatusServer.java ViewLog.java

java -cp ".;lib/sqlite-jdbc-<version>.jar" StatusServer

```

Client (from `client/`):

```

javac HeartbeatClient.java

java HeartbeatClient <server-ip>

```

Open `http://localhost:8080` to see the status.

View the log: `java -cp ".;lib/sqlite-jdbc-<version>.jar" ViewLog`



(On Linux/Mac, use `:` instead of `;` in the classpath.)



\## Concepts covered

TCP and sockets, client-server model, ports, heartbeat and timeout, NAT/private IPs, SQL and JDBC (PreparedStatement), event logging.



\## Testing

Normal connection, client stop, heartbeat timeout, database logging, web page, phone over Wi-Fi, and Wi-Fi drop and recovery (this found a bug, fixed with a reconnect loop). Full details are in `docs/`.



\## Limitations

One client at a time, local network only, no authentication. Multi-device support is planned.

