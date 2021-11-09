/*
Wachirawit  Peerapisarnpon  6213145
Winn        Ladawuthiphan   6213146
*/
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class Bus
{
    private String name;//name bus
    private int seat;//seat bus    
    private int max; //max seat       
     
    class group
    {
        private String name;//name group
        private int seats;//seat group
        
        group(String n,int s)
        {
            name = n;
            seats = s;
        }
    }    
    ArrayList<group> G = new ArrayList();
    
    Bus(int s,int m,String n)
    {
        max = m;
        seat = s;
        name = n;
    }
    
    public <T> T get(String var)
    {        
        if(var.equals("name"))
        {
            return (T) String.valueOf(name);
        }
        else
        {
            return (T) Integer.valueOf(seat);
        }               
    }            
    
    public int PlusSeats(String N,int n)
    {            
        if(seat+n>=max)
        {
            G.add(new group(N,max-seat));
            seat+=n;
            return seat-max;
        }         
        G.add(new group(N,n));
        seat+=n;
        return -1;        
    }
    
    public void PRINT()
    {
        System.out.print(Thread.currentThread().getName()+" >> "+name+" : ");        
        for(int i=0;i<G.size();i++)
        {
            System.out.printf("%-18s (%-3d seats)",G.get(i).name,G.get(i).seats);
            if(i!=G.size()-1)
            {
                System.out.print(",  ");
            }
        }
        System.out.println("");
    }
}
    
class Group
{
    private int id;
    private String name;
    private int numberPassenger;    
    private char destination;
    
    Group(int i,String n,int no,char d)
    {
        id = i;
        name = n;
        numberPassenger = no;
        destination = d;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getNumber()
    {
        return numberPassenger;
    }    
    public void setNumber(int n)
    {
        numberPassenger = n;
    }
    
    public char getDestination()
    {
        return destination;
    }
}
    
class TicketCounter extends Thread
{
    ArrayList<Group> G;
    BusLine BLA;
    BusLine BLC;
    int checkPoint;
    protected CyclicBarrier finish;   
    
    TicketCounter(BusLine bl,BusLine bl2,int c,int i)
    {
        BLA = bl;
        BLC = bl2;
        checkPoint = c;
        
        try
        {
            Scanner read = new Scanner(new File("T"+i+ ".txt"));
            G = new ArrayList();
            
            while (read.hasNext()) 
            {
                String line = read.nextLine();
                String buff[] = line.split(",");
                int id = Integer.parseInt(buff[0].trim());
                String name = buff[1].trim();
                int noPassenger = Integer.parseInt(buff[2].trim());
                char destination = buff[3].trim().charAt(0);
                G.add(new Group(id, name, noPassenger, destination));
            }            
            this.setName("T"+i);

            read.close();
        } 
        catch (FileNotFoundException ex) 
        {
            System.err.println("Error Can't Open File T"+i);
            System.exit(0); 
        } 
    }
    
    void setCyclicBarrier(CyclicBarrier f)
    { 
        finish = f;        
    }
    
    @Override
    public void run()
    {
        for(int i=0;i<G.size();i++)
        {
            switch(G.get(i).getDestination())
            {
                case 'A' :
                {
                    BLA.allocateBus(G.get(i),i+1);
                    break;
                }
                case 'C' :
                {
                    BLC.allocateBus(G.get(i),i+1);
                    break;
                }                    
            }
            
            if(i==checkPoint-2)
            {
                try 
                { 
                    finish.await(); 
                    finish.await();                    
                } 
                catch(Exception ex) 
                { 
                    //do nothing
                }
            }
        }
    }    
}
    
class BusLine
{   
    private ArrayList<Bus> B;
    private int max;
    private char destination;
    BusLine(int m,char n)
    {
        max=m;  
        destination = n;
        B= new ArrayList();
        B.add(new Bus(0,max,n+"0"   ));
    }
    
    public ArrayList<Bus> getbus()
    {
        return B;
    }
    
    public void deleteEmpty()
    {
        if(B.get(B.size()-1).get("seat")==Integer.valueOf(0))
            B.remove(B.size()-1);
        //System.out.println(BC.size());       
    }
    
    synchronized void allocateBus(Group G,int i)
    {
        int reallocate = 1;
        
        while (reallocate == 1) 
        {
            int check = B.get(B.size() - 1).PlusSeats(G.getName(), G.getNumber());
            //System.out.printf("%s >> Transaction %d : ", Thread.currentThread().getName(), i);

            switch (check) 
            {
                case -1: 
                {
                    System.out.printf("%s >> Transaction %d :%-18s (%-3d seats) bus %s\n", Thread.currentThread().getName(), i, G.getName(), G.getNumber(), B.get(B.size() - 1).get("name"));
                    reallocate = 0;
                    break;
                }

                default: 
                {
                    System.out.printf("%s >> Transaction %d :%-18s (%-3d seats) bus %s\n", Thread.currentThread().getName(), i, G.getName(), G.getNumber() - check, B.get(B.size() - 1).get("name"));
                    if (check != 0) {
                        B.add(new Bus(0, max, destination+ "" + B.size()));
                        G.setNumber(check);
                    } else {
                        B.add(new Bus(0, max, destination+ "" + B.size()));
                        reallocate = 0; //edit           
                    }
                }
            }
        }

    }    
}

public class Simulation 
{   

    public static void main(String args[]) 
    {
        TicketCounter T[] = new TicketCounter[3];//Array of Thread
        
        Scanner input = new Scanner(System.in);
        
        System.out.printf("%s >> Enter max seats  = \n",Thread.currentThread().getName());
        int maxSeats = input.nextInt();//Input
        
        System.out.printf("%s >> Enter checkpoint = \n",Thread.currentThread().getName());
        int checkPoint = input.nextInt();//Input
        
        /*
        ArrayList<Bus> BA = new ArrayList();//ArrayList of Bus A
        BA.add(new Bus(0,maxSeats,"A0"));
        
        ArrayList<Bus> BC = new ArrayList();//ArrayList of Bus C
        BC.add(new Bus(0,maxSeats,"C0"));
        */
        
        BusLine BLA = new BusLine(maxSeats,'A');
        BusLine BLC = new BusLine(maxSeats,'C'); 
                
        CyclicBarrier finish = new CyclicBarrier(4);        

        input.close();       
         
        for(int i=0;i<3;i++)//Constructor
        {
            T[i] = new TicketCounter(BLA,BLC,checkPoint,i+1);
        }
        
        for(int i=0;i<3;i++)
        {
            T[i].setCyclicBarrier(finish);
            T[i].start();            
        }
        
        try 
        {
            finish.await();

            if (BLA.getbus().get(BLA.getbus().size()-1).get("seat") == Integer.valueOf(0)) 
                System.out.printf("%s >> %d airport-bound buses have been allocated\n", Thread.currentThread().getName(), BLA.getbus().size() - 1);
            else             
                System.out.printf("%s >> %d airport-bound buses have been allocated\n", Thread.currentThread().getName(), BLA.getbus().size());
            
            if (BLC.getbus().get(BLC.getbus().size()-1).get("seat") == Integer.valueOf(0))             
                System.out.printf("%s >> %d city-bound buses have been allocated\n", Thread.currentThread().getName(), BLC.getbus().size() - 1);            
            else           
                System.out.printf("%s >> %d city-bound buses have been allocated\n", Thread.currentThread().getName(), BLC.getbus().size());            

            finish.await();            
        } 
        catch (Exception e) 
        {
            System.out.println(e);
        }           
        
        try//safety
        {
            for(int i=0;i<3;i++)
                T[i].join();
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }        
        
        BLA.deleteEmpty();
        
        System.out.println("");
        System.out.println(Thread.currentThread().getName()+" >> ===== Airport-Bound =====");
        for(int i=0;i<BLA.getbus().size();i++)
        {
            BLA.getbus().get(i).PRINT();            
        }
        
        System.out.println("");
        System.out.println(Thread.currentThread().getName()+" >> ===== City-Bound =====");
        for(int i=0;i<BLC.getbus().size();i++)
        {            
            BLC.getbus().get(i).PRINT();         
        }
    }
}
