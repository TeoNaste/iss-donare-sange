package donation.utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IObserver extends Remote {

    void addObserver(IObserver observer) throws RemoteException;

    void removeObserver(IObserver observer) throws  RemoteException;

    void testUpdate() throws  RemoteException;

    void notifyDonorAnalyseFinished(String username,String message) throws  RemoteException;

    void notifyDonorUpdateHistory(String username) throws  RemoteException;

    void notifyNewRequestAdded(String username,String message) throws  RemoteException;
}

