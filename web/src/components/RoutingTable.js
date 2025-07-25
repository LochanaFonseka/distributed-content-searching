import React, { useState, useEffect } from "react";
import { getRoutingTable } from "../api";

const RoutingTable = () => {
  const [table, setTable] = useState(null);

  const fetchRoutingTableData = async () => {
    try {
      const response = await getRoutingTable();
      setTable(response.data);
    } catch (error) {
      console.error("Error fetching routing table:", error);
      alert("Failed to fetch routing table");
    }
  };

  useEffect(() => {
    fetchRoutingTableData();
    const interval = setInterval(() => {
      fetchRoutingTableData();
    }, 3000); // Update every 3 seconds

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="routing-table-container">
      <h2 className="routing-table-heading">Routing Table</h2>
      {table ? (
        <div>
          <p className="total-neighbours">Total neighbours: {table.count}</p>
          <table className="routing-table">
            <thead>
              <tr>
                <th>Neighbours No</th>
                <th>Address</th>
                <th>Client Port</th>
              </tr>
            </thead>
            <tbody>
              {table.neighbours.map((neighbour, index) => (
                <tr key={index}>
                  <td>{index + 1}</td>
                  <td>{neighbour.address}</td>
                  <td>{neighbour.clientPort}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
};

export default RoutingTable;
