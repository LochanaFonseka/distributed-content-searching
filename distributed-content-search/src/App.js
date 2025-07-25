import React, { useState } from 'react';
import Unregister from './components/Unregister';
import RoutingTable from './components/RoutingTable';
import SearchFile from './components/SearchFile';
import NodeFiles from './components/NodeFiles';
import './App.css'; // Ensure to link the CSS file for styling

const App = () => {
  const [isUnregistered, setIsUnregistered] = useState(false);

  const handleUnregisterSuccess = () => {
    setIsUnregistered(true);
  };

  return (
    <div className="App">
      <h1>Distributed Content Searching</h1>
      {isUnregistered ? (
        <div className="unregister-success">
          <h2>You have unregistered successfully</h2>
        </div>
      ) : (
        <>
          <Unregister onSuccess={handleUnregisterSuccess} />
          <div className="main-container">
            <div className="section">
              <RoutingTable />
            </div>
            <div className="section node-files-section">
              <NodeFiles />
            </div>
            <div className="section">
              <SearchFile />
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default App;
