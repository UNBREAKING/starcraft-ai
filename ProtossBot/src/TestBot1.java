import java.util.ArrayList;
import java.util.List;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    int NumberOfWorkers=4;
    int MaxNumberOfWorkers=0;
    Unit  WorkerRush=null;
    int sumMin=0;
    int sumGas=0;
    boolean search=false;
 
    Unit Search=null;
    boolean Robotics=false;
    boolean SecondNexus=false;
    boolean Orders1=false;
    public List<Unit> Buildings=new ArrayList<Unit>();
    public List<Unit> GasWorkers=new ArrayList<Unit>();
    public List<Unit> EnemyArmy=new ArrayList<Unit>();
    public List<EnemyBuilding> EnemyBuildings=new ArrayList<EnemyBuilding>();
    public List<EnemyBuilding> EnemyDefenderBuildings=new ArrayList<EnemyBuilding>();
    Gateaways Gates= null;
    int NumberOfCompletedArmy;
    Unit GasMin = null;
    Army army=null;
    Orders MyOrders=null; 
    Searcher MySearcher=null;
    boolean PairOfGates=false;
    Unit forNexus=null;
    Nexus ListOfNexuses=null;
    boolean SCharge=false;
    public NeuronNetwork Control=null;
    
   public class Neuron{
    	double res=0;
    	
    	Neuron(double x, double w,double bias){
    		
    		res=res+x*w+bias;
    		if(res>0){
    			res=1;
    		}else if(res==0){
    			res=0;
    		}else {
    			res=-1;
    		}
    	}
    	
    	public double returnRes(){
    		return res;
    	}
    }
   
   public class NeuronNetwork{
	   public Neuron DPSNeuron=null;
	   public Neuron VisibleEnemies=null;
	   public Neuron Res=null;
	   
	   NeuronNetwork(double DPS,double Army){
		   DPSNeuron=new Neuron(Math.abs(DPS),1,-0.4);
		   VisibleEnemies= new Neuron(Army,1,0);	   
		   Res=new Neuron(DPSNeuron.returnRes()+VisibleEnemies.returnRes(),1,-1);
	   }
	  
	   public double returnRes(){
		   return Res.returnRes();
	   }
	   
   }
    
    public class EnemyBuilding{
    	Unit Building;
    	Position EnemyBuildingPosition;
    	
    	EnemyBuilding(Unit unit){
    	Building=unit;
    	EnemyBuildingPosition=unit.getPosition();
    	} 
    	
    	public Unit returnUnit(){
    		return Building;
    	}
    	public Position returnPosition(){
    		return EnemyBuildingPosition;
    	}
    	
        public boolean isVisible(){
        	return Building.isVisible();
        }
    }
    
    public class Nexus{
    	public List<Unit> Nexuses=new ArrayList<Unit>();
    	boolean toBuild=false;
    	Nexus(){
    		
    	}

    	public void addNexus(Unit Nexus){
    		Nexuses.add(Nexus);
    	}
    	
    	public void buildWorker(){
    		Unit Nexus = Nexuses.get(Nexuses.size()-1);
    		
    		
    		if(toBuild==false){
           for(int i=NumberOfWorkers;i<MaxNumberOfWorkers;i++){
        	   NumberOfWorkers++;
        	   MyOrders.createOrder(Nexus, UnitType.Protoss_Probe);
           }
    		toBuild=true;
    		}
    	}
    }
    
    public class Order{
    	
		Unit worker;
		UnitType building;
		int minerals=0;
		int gas=0;
		boolean Build=false;
		TilePosition FinalPosition=null;
		UpgradeType Up=null;
		
		public Order(UnitType type, Unit worker){
    	 System.out.println("NEW ORDER TO BIULD OR TRAIN: "+type);
    	building=type;
    	this.worker=worker;
    		minerals=type.mineralPrice();
    		gas=type.gasPrice();
    	
    	}
		
		public Order(UpgradeType type, Unit worker){
			System.out.println("NEW ORDER TO UPGRADE: "+type);
		this.worker=worker;
		Up=type;
		minerals=type.mineralPrice();
		gas=type.gasPrice();
		}
		
    	public void toBuild(){
    		if(worker.getType().isWorker()){
    		if(building==UnitType.Protoss_Nexus){
    			findPositionForBase();
    			if(worker.getPosition().equals(FinalPosition.toPosition())){
    			worker.build(building, FinalPosition);
    			Build=true;
    			}
    		}else{	
    		TilePosition buildTile=getBuildTile(worker,building,self.getStartLocation());
    	   	if(buildTile!=null ){
    	   	worker.build(building,buildTile);
    	   	System.out.println("TRUE");
    	   	Build=true;
    	   	}
    		}
    		
    	   	}else{
    	   		worker.train(building);
    	   		Build=true;
    	   		
    	   	}
    	   	
    		
    	}
    	
    	public void toUp(){
    	worker.upgrade(Up);
    	System.out.println("TRUE");
    	Build=true;
    	}
    	
    	public boolean inBuild(){
    		return Build;
    	}
    	
    	
    	public void findPositionForBase(){
    		TilePosition BasicPosition= self.getStartLocation();
    		double Distance=0;
    		if(FinalPosition==null){
    			for (BaseLocation b : BWTA.getBaseLocations()) {
    				if(Distance==0){
    					Distance=Math.sqrt(Math.pow(BasicPosition.getX()-b.getTilePosition().getX(), 2)+Math.pow(BasicPosition.getY()-b.getTilePosition().getY(), 2) );
    				}
				if (!b.isStartLocation() && !self.getStartLocation().equals(b.getTilePosition()) 
				&& Distance>=Math.sqrt(Math.pow(BasicPosition.getX()-b.getTilePosition().getX(), 2)+Math.pow(BasicPosition.getY()-b.getTilePosition().getY(), 2)) ) 
				{
						 Distance=Math.sqrt(Math.pow(BasicPosition.getX()-b.getTilePosition().getX(), 2)+Math.pow(BasicPosition.getY()-b.getTilePosition().getY(), 2) );
						 FinalPosition=b.getTilePosition();
				 }
    		}
    	worker.move(FinalPosition.toPosition());
    	}
    		}
    }
    
    public class Orders{
    	 public List<Order> ordersToBuild =null;
    	 int NumberOfCompletedOreders=1;
    	 int NumberOfCompletedUp=0;
    	Orders(){
    		ordersToBuild=new ArrayList<Order>();
    	}
    	
    	public void createOrder(Unit worker,UnitType Building){
    		Order order= new Order(Building,worker);
        	ordersToBuild.add(order);
    	}
    	
    	public void createOrder(Unit worker,UpgradeType Building){
    		Order order= new Order(Building,worker);
        	ordersToBuild.add(order);
    	}
    	
    	  public void workWithOrders(){
    		
    	    	for (int i=0;i<ordersToBuild.size();i++){
    	    		Order BuildNow= ordersToBuild.get(i);
    	    		if(BuildNow.inBuild()==false  && BuildNow.Up==null &&  BuildNow.minerals<=self.minerals() && BuildNow.gas<=self.gas() && 
    	    				((BuildNow.worker.getType().isWorker()==true && BuildNow.worker.isConstructing()==false && BuildNow.worker.canBuild(BuildNow.building)) 
    	    						|| (BuildNow.worker.getType().isWorker()==false && BuildNow.worker.canTrain(BuildNow.building)))
    	    		&& NumberOfCompletedOreders<=Buildings.size()+NumberOfCompletedArmy+NumberOfCompletedUp && i==Buildings.size()-1+NumberOfCompletedArmy+NumberOfCompletedUp){
    	    			System.out.println("We are building "+BuildNow.building.toString());
    	    			BuildNow.toBuild();
    	    			
    	    			if(BuildNow.inBuild()){
    	    				NumberOfCompletedOreders++;
    	    				
    	    			}	
    	    			
    	    			
    	    			break;
    	    		}else if(BuildNow.inBuild()==false && BuildNow.minerals<=self.minerals() && BuildNow.gas<=self.gas() && BuildNow.Up!=null){
    	    			System.out.println("We are upgrading "+BuildNow.Up.toString());
    	    			BuildNow.toUp();
    	    			if(BuildNow.inBuild()){
    	    				NumberOfCompletedOreders++;
    	    				NumberOfCompletedUp++;
    	    			}	
    	    		}
    	    		else if(NumberOfCompletedOreders>Buildings.size()+NumberOfCompletedArmy+NumberOfCompletedUp){
    	    		break;}
    	    	}
    			
    	    }
    	  
    	  public int allSumMinerals(){
    		  int min=0;
    		  for(int i=NumberOfCompletedOreders-1;i<ordersToBuild.size();i++){Order BuildNow= ordersToBuild.get(i);
    		  min+=BuildNow.minerals;
    		  }
    		  
    		  return min; 
    	  }
    	  public int allSumGas(){
    		  int gas=0;
    		  for(int i=NumberOfCompletedOreders-1;i<ordersToBuild.size();i++){Order BuildNow= ordersToBuild.get(i);
    		  gas+=BuildNow.gas;
    		  }
    		  
    		  return gas; 
    	  }
    }
    
    public void orders(Unit worker){
    	
    	MyOrders.createOrder(worker,UnitType.Protoss_Pylon);
    	MyOrders.createOrder(worker,UnitType.Protoss_Assimilator);
    	MyOrders.createOrder(worker,UnitType.Protoss_Gateway);
    	MyOrders.createOrder(worker,UnitType.Protoss_Forge);
    	MyOrders.createOrder(worker,UnitType.Protoss_Cybernetics_Core);
    	MyOrders.createOrder(worker,UnitType.Protoss_Pylon);
    	
    	
    	
    System.out.println("TRUE");
    }
      
    public void orders1(Unit worker){
    	MyOrders.createOrder(worker,UnitType.Protoss_Gateway);
    	
    	MyOrders.createOrder(worker,UnitType.Protoss_Gateway);
    	MyOrders.createOrder(worker,UnitType.Protoss_Pylon);
    	MyOrders.createOrder(worker,UnitType.Protoss_Photon_Cannon);
    }
    
    public class Gateaways{
    	public List<Unit> Gates;
    	int i=0;
    	int index=0;
    	Gateaways(){
    		Gates=new ArrayList<Unit>();
    	}
    	
    	public void add(Unit unit){
    		Gates.add(unit);
    		System.out.println(Gates.size());
    	}
    	
    	public int isInList(Unit unit){
    		return Gates.indexOf(unit);
    	}
    	
    	public void buildUnit(UnitType unit){
    		
    		
    		if(Gates.size()>0){
    			Unit gate=Gates.get(i);
    			if(unit.mineralPrice()<=self.minerals() && unit.gasPrice()<=self.gas() && MyOrders.allSumMinerals()<=self.minerals() && MyOrders.allSumGas()<=self.gas()
    					&& self.supplyTotal()/2-index-unit.supplyRequired()/2>=self.supplyUsed()/2)
    				{
    				MyOrders.createOrder(gate, unit);
    				
    				i++;
    				index+=unit.supplyRequired()/2;
    				
    				}
    			if(i==Gates.size()){
    				i=0;
    			}
    		}
    		
    	}
    }
    
    public class Searcher{
    	Unit searcher=null;
    	
    	Searcher(Unit s){
    		searcher=s;
    	}
    	
    	public void intelligence(){
    		for (BaseLocation b : BWTA.getBaseLocations()) {
				
				if (b.isStartLocation() && !self.getStartLocation().equals(b.getTilePosition()) && !searcher.getPosition().equals(b.getPosition())) {
					searcher.move(b.getPosition());		 
				 }
    		}
    	}
    	
    }
    
    public class Army{
    	public List<Unit> army= null;
    	boolean inAttack=false;
    	boolean inAttackBuilding=false;
    	int armysize=0;
    	boolean inAttackDefBuilding=false;
    	int num=0;
    	double DPSofArmy=0;
		double HPofArmy=0;
		double DPSofEnemyArmy=0;
		double HPofEnemyArmy=0;
		
    	Army(){
    		army=new ArrayList<Unit>();
    		army.clear();
    		}
    	public void add(Unit unit){
    		army.add(unit);
    		inAttack=false;
    	}
    	public List<Unit> returnList(){
    		return army;
    	}
    	
    	public void delete(Unit unit){
    		army.remove(unit);
    	}
    	
    	public void attackEnemyBase(){
    		
    		if (army.size()>=16){
    			for (BaseLocation b : BWTA.getBaseLocations()) {
    				
    				if (b.isStartLocation() && !self.getStartLocation().equals(b.getTilePosition())) {
    					for (int j=0;j<army.size();j++){
    				 army.get(j).attack(b.getPosition());
    				 }
    				}
    			}
    		}
    		
    	}
    	
    	public void returnToBase(){
    		for (int j=0;j<army.size();j++){
				army.get(j).move(self.getStartLocation().toPosition());
				
    		}
    	}
    	
    	public void adddpsAndHp(double dps,double hp){
    		 DPSofArmy+=dps;
    		 HPofArmy+=hp;
    	}
    	public void WriteDPSandHPofAll(){
    		
    		HPofArmy=0;
    		for(int i=0;i<army.size();i++){
    			HPofArmy+=army.get(i).getHitPoints();
    			
    		}
    		
    		System.out.println("DPSofMyArmy:"+DPSofArmy+" HPofMyArmy:"+HPofArmy);	
    		System.out.println("DPSofEnemyArmy:"+DPSofEnemyArmy+" HPofenemyArmy:"+HPofEnemyArmy);
   	}
    	
    	
    	public int visibleEnemies(){
    		num=0;
    		for(int i=0;i<EnemyArmy.size();i++){
    			if(!EnemyArmy.get(i).isVisible()){
    				num++;
    			}
    		}
    		
    		
    		return num;
    	}
    	
    	
    	public int visibleEnemiesDB(){
    		int Num=0;
    		for(int i=0;i<EnemyDefenderBuildings.size();i++){
    			if(!EnemyDefenderBuildings.get(i).isVisible()){
    				Num++;
    			}
    		}
    		
    		
    		return Num;
    	}
    	
public void attackEnemyBuildings(){
    		
			if(armysize!=army.size()){
				inAttackBuilding=false;
				armysize=army.size();
			}
    		
    		for(int i=0;i<EnemyBuildings.size();i++){
    			EnemyBuilding Enemy=EnemyBuildings.get(i);
    			if(inAttackBuilding==false){
    			for (int j=0;j<army.size();j++){
    				army.get(j).attack(Enemy.returnPosition());
    				
    			}
    			inAttackBuilding=true;
    			}
    			if(EnemyBuildings.contains(Enemy)){
    				break;
    			}else{
    				inAttackBuilding=false;
    			}
    		}
    		
    		
		
		
    		
    	}
    	
    	public void attackEnemyDefBuildings(){
    		
    		for(int i=0;i<EnemyDefenderBuildings.size();i++){
    			EnemyBuilding Enemy=EnemyDefenderBuildings.get(i);
    			if(inAttackDefBuilding==false){
    			for (int j=0;j<army.size();j++){
    				army.get(j).attack(Enemy.returnPosition());
    				
    			}
    			inAttackDefBuilding=true;
    			}
    			if(EnemyDefenderBuildings.contains(Enemy)){
    				break;
    			}else{
    				inAttackDefBuilding=false;
    			}
    		}
    		
    	}
    	
    	public void attack(){
    		
    		
    		
    		if(EnemyArmy.size()==visibleEnemies() || armysize!=army.size()){
    			inAttack=false;
    			armysize=army.size();
    		}
    		
    		if(inAttack==false)
    		for (int i=0;i<army.size();i++){
    			Unit MyUnit=army.get(i);
    			Unit Enemy=EnemyArmy.get(0);
    			for(int j=1;j<EnemyArmy.size();j++){
    				if(Math.abs(
    						Math.pow(MyUnit.getTilePosition().getX()-Enemy.getTilePosition().getX(),2)+ Math.pow(MyUnit.getTilePosition().getY()-Enemy.getTilePosition().getY(),2)
    						)>Math.abs(
    	    						Math.pow(MyUnit.getTilePosition().getX()-EnemyArmy.get(j).getTilePosition().getX(),2)+ Math.pow(MyUnit.getTilePosition().getY()-EnemyArmy.get(j).getTilePosition().getY(),2)
    	    						)
    						){
    					Enemy=EnemyArmy.get(j);
    			}
    			
    		}
    			MyUnit.attack(Enemy);
    			inAttack=true;	
    		}
    		
    		
    		
    		
    	}
    	
    	
    	 public void findEnemies(){
    	    	//add enemy army
    	    	for (Unit u:game.enemy().getUnits()){
    	        	if(!u.getType().isBuilding() && EnemyArmy.indexOf(u)==-1 && u.getType()!=UnitType.Unknown){
    	        		EnemyArmy.add(u);
    	        		double unitDamage;
    	        		double unitWeaponCooldown;
    	        		if(u.getType()!=UnitType.Terran_Medic && !u.getType().isWorker() ){
    	        		if(u.getType().isFlyer()){
    	        			unitDamage=u.getType().airWeapon().damageAmount();
    	        			unitWeaponCooldown=42*(double)u.getType().airWeapon().damageCooldown()/1000;
    	        		}else{
    	        			unitDamage=u.getType().groundWeapon().damageAmount();
    	        			unitWeaponCooldown=42*(double)u.getType().groundWeapon().damageCooldown()/1000;
    	        		}
    	        		System.out.println(u.getType());
    	        		DPSofEnemyArmy+=unitDamage/unitWeaponCooldown;
    	        		HPofEnemyArmy+=u.getType().maxHitPoints();
    	        		WriteDPSandHPofAll();
    	        		}
    	        		
    	        		
    	        	}
    	        	//add enemy not defender buildings
    	        	if(u.getType().isBuilding() ){
    	        	int in=0;	
    	        	for(int i=0;i<EnemyBuildings.size();i++ ){
    	        		EnemyBuilding EB=EnemyBuildings.get(i);
    	        		if(EB.returnUnit().equals(u)){
    	        			in=1;
    	        		}
    	        	}
    	        	if(in==0){
    	        		EnemyBuildings.add(new EnemyBuilding(u));
    	        	}	
    	        		
    	        	}
    	        	
    	        	
    	        }
    	    	 for (Unit Enemy:EnemyArmy){
    	         	boolean isOnMap=false;
    	         	for (Unit u:game.enemy().getUnits())
    	         	{
    	         		if(Enemy.equals(u)){
    	         			isOnMap=true;
    	         		}
    	         	}
    	         	if(isOnMap==false){
    	             	EnemyArmy.remove(Enemy);
    	             	double unitDamage;
    	        		double unitWeaponCooldown;
    	        		if(Enemy.getType()!=UnitType.Terran_Medic && !Enemy.getType().isWorker()){
    	        		if(Enemy.getType().isFlyer()){
    	        			unitDamage=Enemy.getType().airWeapon().damageAmount();
    	        			unitWeaponCooldown=42*(double)Enemy.getType().airWeapon().damageCooldown()/1000;
    	        		}else{
    	        			unitDamage=Enemy.getType().groundWeapon().damageAmount();
    	        			unitWeaponCooldown=42*(double)Enemy.getType().groundWeapon().damageCooldown()/1000;
    	        		}
    	        		DPSofEnemyArmy-=unitDamage/unitWeaponCooldown;
    	        		HPofEnemyArmy-=Enemy.getType().maxHitPoints();
    	             	}}
    	    }
    	    	 for(int i=0;i<EnemyBuildings.size();i++){
    	    		 boolean isOnMap=false;
    	    		 EnemyBuilding EB=EnemyBuildings.get(i);
    	    		 for (Unit u:game.enemy().getUnits()){
    	    		 if(EB.returnUnit().equals(u)){
    	  			isOnMap=true;
    	    		 }
    	    		 }
    	    		 if(isOnMap==false){
    	    	         	EnemyBuildings.remove(EB);
    	    	         	}
    	    	 }
    	    	
    	    
    	    }
    
    
    }
    
  
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
        
        if(!unit.getType().isBuilding() && self.supplyTotal()/2!=9 && self.supplyTotal()/2-unit.getType().supplyRequired()/2<=self.supplyUsed()/2 ){
        	MyOrders.createOrder(WorkerRush,UnitType.Protoss_Pylon);
            }
        if(!unit.getType().isBuilding() && !unit.getType().isWorker() && army.returnList().indexOf(unit)==-1 &&  unit.getType()!=UnitType.Critter_Scantid && unit.getType()!=UnitType.Protoss_Observer){
        	army.add(unit);
        	double unitDamage=0;double unitWeaponCooldown=0;
        	if(unit.getType().isFlyer()){
    			unitDamage=unit.getType().airWeapon().damageAmount();
    			unitWeaponCooldown=42*(double)unit.getType().airWeapon().damageCooldown()/1000;
    		}else{
    			unitDamage=unit.getType().groundWeapon().damageAmount();
    			unitWeaponCooldown=42*(double)unit.getType().groundWeapon().damageCooldown()/1000;
    		}
        	army.adddpsAndHp(unitDamage/unitWeaponCooldown, 0);
        	army.WriteDPSandHPofAll();
        	
        	
        	NumberOfCompletedArmy++;
        	if(unit.getType()==UnitType.Protoss_Dragoon){
    			Gates.index-=unit.getType().supplyRequired()/2;
    		}
        	
        	
        }
        if(unit.getType()==UnitType.Protoss_Observer){
        	NumberOfCompletedArmy++;
        }
        if(unit.getType()==UnitType.Protoss_Nexus){
        	MaxNumberOfWorkers+=9;
        	ListOfNexuses.addNexus(unit);
        	ListOfNexuses.toBuild=false;
        	System.out.println("Position is found");
        }
        if(unit.getType().isWorker()){
        	NumberOfCompletedArmy++;
        }
       
    }
    
    public void onUnitDestroy(Unit unit){
    	if(army.returnList().indexOf(unit)!=-1){
    		army.delete(unit);
    		double unitDamage=0;double unitWeaponCooldown=0;
        	if(unit.getType().isFlyer()){
    			unitDamage=unit.getType().airWeapon().damageAmount();
    			unitWeaponCooldown=42*(double)unit.getType().airWeapon().damageCooldown()/1000;
    		}else{
    			unitDamage=unit.getType().groundWeapon().damageAmount();
    			unitWeaponCooldown=42*(double)unit.getType().groundWeapon().damageCooldown()/1000;
    		}
        	army.adddpsAndHp(-unitDamage/unitWeaponCooldown,-180);
    		
    	}
    	if(unit.getType().isWorker() && self.getUnits().indexOf(unit)!=-1){
    		NumberOfWorkers--;
    	}
    	if (unit.equals(WorkerRush)){
    		WorkerRush=null;
    		NumberOfWorkers--;
    	}
    	if (unit.getType()==UnitType.Protoss_Observer){
    		Search=null;
    		search=false;
    		MySearcher=null;
    		System.out.println("Searcher is DEAD");
    				
    	}
    }
	
    
    @Override
    public void onStart() {
    	Orders1=false;
    	PairOfGates=false;
    	NumberOfWorkers=4;
        MaxNumberOfWorkers=0;
        EnemyBuildings=new ArrayList<EnemyBuilding>();
        EnemyDefenderBuildings=new ArrayList<EnemyBuilding>();
        WorkerRush=null;
        MyOrders=new Orders();
        sumMin=0;
        sumGas=0;
        Buildings=new ArrayList<Unit>();
        NumberOfCompletedArmy=-4;
        GasWorkers=new ArrayList<Unit>();
        GasMin = null;
        Gates= new Gateaways();
        army =new Army();
        EnemyArmy=new ArrayList<Unit>();
        search=false;
        SecondNexus=false;
        Search=null;
        MySearcher=null;
        Robotics=false;
        forNexus=null;
        SCharge=false;
        game = mirror.getGame();
        self = game.self();
        game.setLocalSpeed(10);
        ListOfNexuses= new Nexus();
        Control=null;
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }
        
        
       

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        
        ListOfNexuses.buildWorker();
        
        if(MaxNumberOfWorkers==14 && Buildings.size()==5 && Orders1==false){
        	Orders1=true;
        	orders1(WorkerRush);
        }
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            
            if (myUnit.getType().isBuilding()==true && Buildings.indexOf(myUnit)==-1){
            	Buildings.add(myUnit);
            }
            
            if(myUnit.getType()==UnitType.Protoss_Gateway && Gates.isInList(myUnit)==-1){
            	Gates.add(myUnit);
            	
            }
          if(army.army.size()>10 && WorkerRush!=null && Robotics==false){  
          MyOrders.createOrder(WorkerRush,UnitType.Protoss_Robotics_Facility);
          MyOrders.createOrder(WorkerRush,UnitType.Protoss_Observatory);
          
          Robotics=true;
          }
            if(myUnit.getType()==UnitType.Protoss_Robotics_Facility && search==false){
            	
            	MyOrders.createOrder(myUnit, UnitType.Protoss_Observer);
            	search=true;
            	
            	
            	
            }
            if(Search==null && myUnit.getType()==UnitType.Protoss_Observer){
            	Search=myUnit;
            	MySearcher=new Searcher(Search);
            	if(PairOfGates==false){
            	
            	PairOfGates=true;
            	}
            	System.out.println("Searcher is Found");
            	
            }
            
            if(myUnit.getType()==UnitType.Protoss_Cybernetics_Core && SCharge==false){
            	
            MyOrders.createOrder(myUnit,UpgradeType.Singularity_Charge );
            SCharge=true;
            }
            	
            
          
            
            if(myUnit.getType().isWorker() && WorkerRush==null){
            	WorkerRush=myUnit;
   				orders(WorkerRush);
            }
            //if it's a worker and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
                Unit closestMineral = null;
                
                if(myUnit!=WorkerRush && GasWorkers.size()<3){
                	GasWorkers.add(myUnit);
                }
                
                //find the closest mineral
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                    
                    }
                
                
            }
            
            if (myUnit.getType()==UnitType.Protoss_Assimilator && MaxNumberOfWorkers==9 && Buildings.size()==5){
            	
            	MaxNumberOfWorkers+=5;
            	ListOfNexuses.toBuild=false;
            	for(Unit Gas:GasWorkers){
            		Gas.gather(myUnit,false);
            	}
            }
            
        }
        
        MyOrders.workWithOrders();
       if(MySearcher!=null){ 
       MySearcher.intelligence();
       }
       
      
       Gates.buildUnit(UnitType.Protoss_Dragoon);
       
        int InvisibleEnemies=army.visibleEnemies();
       
        army.findEnemies();
        
        Control=new NeuronNetwork(army.DPSofArmy/army.DPSofEnemyArmy,EnemyArmy.size()-InvisibleEnemies);
        
        if(Control.returnRes()==1
        		){
        	
        army.attack();
        army.inAttackDefBuilding=false;
        army.inAttackBuilding=false;
        }
        else if(Control.returnRes()==0) {
        army.attackEnemyBuildings(); 
        army.inAttackDefBuilding=false;
        army.inAttack=false;
        }else{
        	army.returnToBase();
        }
        
        
        
      
    }

    
    
    public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
     	TilePosition ret = null;
     	int maxDist = 6;
     	int stopDist = 40;
     	
     	// Refinery, Assimilator, Extractor
     	if (buildingType.isRefinery()) {
     		for (Unit n : game.neutral().getUnits()) {
     			if ((n.getType() == UnitType.Resource_Vespene_Geyser) && 
     					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
     					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
     					) return n.getTilePosition();
     		}
     	}
     	
     	while ((maxDist < stopDist) && (ret == null)) {
     		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
     			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
     				if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
     					// units that are blocking the tile
     					boolean unitsInWay = false;
     					for (Unit u : game.getAllUnits()) {
     						if (u.getID() == builder.getID()) continue;
     						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
     					}
     					if (!unitsInWay) {
     						return new TilePosition(i, j);
     					}
     					// creep for Zerg
     					if (buildingType.requiresCreep()) {
     						boolean creepMissing = false;
     						for (int k=i; k<=i+buildingType.tileWidth(); k++) {
     							for (int l=j; l<=j+buildingType.tileHeight(); l++) {
     								if (!game.hasCreep(k, l)) creepMissing = true;
     								break;
     							}
     						}
     						if (creepMissing) continue; 
     					}
     				}
     			}
     		}
     		maxDist += 2;
     	}
     	
     	if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
     	return ret;
     }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}