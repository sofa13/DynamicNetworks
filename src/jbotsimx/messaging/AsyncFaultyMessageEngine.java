/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbotsimx.messaging;

import jbotsim.Message;

/**
 * This asynchronous messages engine add an adversary that destroies messages.
 * 
 * 
 * 
 * @author giuseppe
 */
public class AsyncFaultyMessageEngine extends AsyncMessageEngine{

    double degrate;
    
    
    
    public AsyncFaultyMessageEngine(double degrate,double averageDuration, Type type) {

        super(averageDuration, type);
        this.degrate=degrate;

    }
    
    @Override
    protected void deliverMessage(Message m){

       double x=r.nextDouble();
       if(x > this.degrate){
           
           super.deliverMessage(m);
       }
        
    }
    
   
    
}
