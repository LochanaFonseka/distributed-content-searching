import React, { useState } from "react";
import Unregister from "./components/Unregister";
import RoutingTable from "./components/RoutingTable";
import SearchFile from "./components/SearchFile";
import NodeFiles from "./components/NodeFiles";
import "./App.css";

const App = () => {
  const [isUnregistered, setIsUnregistered] = useState(false);

  const handleUnregisterSuccess = () => {
    setIsUnregistered(true);
  };

  return (
    <div className="App">
      <header className="app-header">
        <h1 className="title">📁 Distributed File Content Search System</h1>
        {!isUnregistered && (
          <div className="unregister-icon">
            <Unregister onSuccess={handleUnregisterSuccess} />
          </div>
        )}
      </header>

      <main className="app-main-layout">
        {isUnregistered ? (
          <div className="unregister-success">
            <h2>✅ You have unregistered successfully</h2>
          </div>
        ) : (
          <>
            <section className="content-row">
              <div className="half-section">
                <NodeFiles />
              </div>
              <div className="half-section">
                <SearchFile />
              </div>
            </section>
            <footer className="routing-footer">
              <RoutingTable />
            </footer>
          </>
        )}
      </main>
    </div>
  );
};

export default App;
