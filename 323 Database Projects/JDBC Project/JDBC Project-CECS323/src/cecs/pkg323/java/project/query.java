/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs.pkg323.java.project;
import java.sql.PreparedStatement;
import java.util.*;
/**
 *
 * @author Greg
 */
public class query {
    public query(){
        query = new ArrayList<String>();
    }
    public String list(){
        String q = "SELECT * FROM ?;";
        return q;
    }
    public void add(String q){
        query.add(q);
    }
    public void remove(int i){
        query.remove(i);
    }
    private ArrayList<String> query;
}
