import React from "react";
import { nodeUnregister } from "../api";

const Unregister = ({ onSuccess }) => {
  const handleNodeUnregister = async () => {
    try {
      const response = await nodeUnregister();
      alert(response.data);
      onSuccess(); // Call the success callback
    } catch (error) {
      console.error(error);
      alert("Unregistration failed");
    }
  };

  return (
    <div>
      <button className="unregister-button" onClick={handleNodeUnregister}>
        Unregister
      </button>
    </div>
  );
};

export default Unregister;
