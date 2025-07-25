import React from 'react';
import { unregister } from '../api';

const Unregister = ({ onSuccess }) => {
  const handleUnregister = async () => {
    try {
      const response = await unregister();
      alert(response.data);
      onSuccess(); // Call the success callback
    } catch (error) {
      console.error(error);
      alert('Unregistration failed');
    }
  };

  return (
    <div>
      <button className="unregister-button" onClick={handleUnregister}>
        Unregister
      </button>
    </div>
  );
};

export default Unregister;
