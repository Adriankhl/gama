model boids 

global torus: torus_environment{ 
	int number_of_agents parameter: 'Number of agents' <- 10 min: 1 max: 1000000;
	int number_of_obstacles parameter: 'Number of obstacles' <- 4 min: 0;
	int boids_size parameter: 'Boids size' <- 20 min: 1;
	float maximal_speed parameter: 'Maximal speed' <- 15.0 min: 0.1 max: 15.0;
	int  radius_speed parameter: 'radius speed' <- 1 min: 1;
	int cohesion_factor parameter: 'Cohesion Factor' <- 200;
	int alignment_factor parameter: 'Alignment Factor' <- 100; 
	float minimal_distance parameter: 'Minimal Distance' <- 10.0; 
	int maximal_turn parameter: 'Maximal Turn' <- 90 min: 0 max: 359; 
	int width_and_height_of_environment parameter: 'Width/Height of the Environment' <- 800;  
	bool torus_environment parameter: 'Toroidal Environment ?' <- false; 
	bool apply_cohesion <- true parameter: 'Apply Cohesion ?';
	bool apply_alignment <- true parameter: 'Apply Alignment ?';   
	bool apply_separation <- true parameter: 'Apply Separation ?';   
	bool apply_goal <- true parameter: 'Follow Goal ?'; 
	bool apply_avoid <- true parameter: 'Apply Avoidance ?';   
	bool apply_wind <- true parameter: 'Apply Wind ?';     
	bool moving_obstacles <- false parameter: 'Moving Obstacles ?';    
	int bounds <- int(width_and_height_of_environment / 20); 
	point wind_vector <- {0,0} parameter: 'Direction of the wind';  
	int goal_duration <- 30 value: (goal_duration - 1); 
	point goal <- {rnd (width_and_height_of_environment - 2) + 1, rnd (width_and_height_of_environment -2) + 1 }; 
	list images of: file <- [file('../images/bird1.png'),file('../images/bird2.png'),file('../images/bird3.png')]; 
	int xmin <- bounds;    
	int ymin <- bounds;  
	int xmax <- (width_and_height_of_environment - bounds);    
	int ymax <- (width_and_height_of_environment - bounds);   
	geometry shape <- square(width_and_height_of_environment);


	// flock's parameter 
	const two_boids_distance type: float init: 30.0;  
	const merging_distance type: int init: 30;
	var create_flock type: bool init: false;  
	
	init {
		create boids number: number_of_agents { 
			location <- {rnd (width_and_height_of_environment - 2) + 1, rnd (width_and_height_of_environment -2) + 1 };
		} 
		 
		create obstacle number: number_of_obstacles {
			location <- {rnd (width_and_height_of_environment - 2) + 1, rnd (width_and_height_of_environment -2) + 1 }; 
		}
		
		create  boids_goal number: 1 {
			location <- goal;
		}
		
		create aggregatedboids;
	}
	
		
	 reflex create_flocks {
	 	if create_flock {
	 		let potentialBoidsNeighboursMap type: map value: ([] as map);
	 		
	 		loop one_boids over: boids {
	 			list<boids> free_neighbours <- boids overlapping (one_boids.shape + (float (two_boids_distance)));
	 			remove one_boids from: free_neighbours;  

	 			if !(empty (free_neighbours)) {
	 				add (one_boids::free_neighbours) to: potentialBoidsNeighboursMap;
	 			} 
	 		}
	 		
	 		list<boids> sorted_free_boids <- (potentialBoidsNeighboursMap.keys) sort_by (length (list (potentialBoidsNeighboursMap at (boids (each)))));
	 		loop one_boids over: sorted_free_boids {
	 			list<boids> one_boids_neighbours <- list(potentialBoidsNeighboursMap at one_boids);
	 			
	 			if  (one_boids_neighbours != nil) {
	 				loop one_neighbour over: one_boids_neighbours {
	 					remove one_neighbour from: potentialBoidsNeighboursMap; 
	 				}
	 			}
	 		}
	 		
		 	list<boids> boids_neighbours <- (potentialBoidsNeighboursMap.keys);
		 	loop one_key over: boids_neighbours {
		 		put (remove_duplicates ((list (potentialBoidsNeighboursMap at (one_key))) + one_key)) at: one_key in: potentialBoidsNeighboursMap;
		 	}
		 	
		 	loop one_key over: (potentialBoidsNeighboursMap.keys) {
		 		list<boids> micro_agents <- list(potentialBoidsNeighboursMap at one_key);
		 			
		 		if ( (length (micro_agents)) > 1 ) {
		 			create flock number: 1 with: [ color::rgb([rnd (255), rnd (255), rnd (255)]) ] { 
		 				capture micro_agents as: boids_delegation;
		 			}
		 		}
		 	} 
		}
	}  
}

entities {
	species name: boids_goal skills: [moving] {
		const range type: float init: 20.0;
		const size type: float init: 10.0;
		int radius <-3.5;
		
		/*reflex wander { 
			do wander amplitude: 45 speed: 20;  
			goal <- location;
		}*/
		
		reflex wander_in_circle{

			set location <- {world.shape.width/2 + world.shape.width/2 * cos (time*radius_speed), world.shape.width/2 + world.shape.width/2 * sin (time*radius_speed)};
			goal <- location;
		}
		
		aspect default {
			draw circle(10) color: rgb ('red');
			draw circle(40) color: rgb ('orange') size: 40 empty: true;
		}
		
		aspect sphere{
			draw sphere(10) color: rgb('white');
		}
	} 
	
	species flock  {
		float cohesionIndex <- two_boids_distance update: (two_boids_distance + (length (members)));
		rgb color <- rgb ([64, 64, 64]);
	 	geometry shape update: !(empty (members)) ? ( (polygon (members collect (boids_delegation (each)).location )) + 2.0 ) : ( polygon ([ {rnd (width_and_height_of_environment), rnd (width_and_height_of_environment)} ]) );
		 
 
		species boids_delegation parent: boids topology: topology(world.shape)  {
			list<boids> others -> {( (boids_delegation overlapping (shape + range))) - self};

			action compute_mass_center type: point {
				loop o over: others {
					if condition: dead(o) { // �a peut faire lever un message "warning" dans la vue "Errors" 
						do write message: 'in ' + name + ' agent with others contains death agents'; 
					} 
				}
				 
				return (length(others) > 0) ? ((mean (others collect (each.location)) ) as point) : location;
			}

			reflex separation when: apply_separation {
			}
			
			reflex alignment when: apply_alignment {
			}
			
			reflex cohesion when: apply_cohesion {
				point acc <- ((self compute_mass_center []) as point) - location;
				acc <- acc / cohesion_factor;
				velocity <- velocity + acc;
			}
			
			reflex avoid when: apply_avoid {
			}		
		}
		
		reflex capture_release_boids {
			 list<boids_delegation> removed_components <- boids_delegation where ((each distance_to location) > cohesionIndex );
			 if !(empty (removed_components)) {
			 	release removed_components;
			 }
			 
			 list<boids> added_components <- boids where ((each distance_to location) < cohesionIndex );
			 if !(empty (added_components)) {
			 	capture added_components as: boids_delegation;
			 }
		}
		
		reflex dispose when: ((length (members)) < 2) {
			 release members;
			 do die;
		}
		
		reflex merge_nearby_flocks {
			list<flock> nearby_flocks<- (flock overlapping (shape +  merging_distance));
			if !(empty (nearby_flocks)) {
			 	nearby_flocks <- nearby_flocks sort_by (length (each.members));
			 	let largest_flock <- nearby_flocks at ((length (nearby_flocks)) - 1);
			 	 
			 	remove largest_flock from: nearby_flocks;
			 	 
			 	list<boids> added_components <- [];
			 	loop one_flock over: nearby_flocks {
			 		release one_flock.members returns: released_boids; 
			 		
			 		loop rb over: list(released_boids) {
			 			add boids(rb) to: added_components;
			 		}
			 	}
			 	
			 	if !(empty (added_components)) { 
			 		ask largest_flock {
			 			capture added_components as: boids_delegation;
			 		}
			 	} 
			 }
		}
		
		aspect default {
			draw shape color: color;
		}
	}
	
	
	species aggregatedboids{
		aspect base{   
			point loc <- (mean (boids collect (each.location)) ) as point;
			draw sphere(10) color: rgb('red') at:loc;
		}
	}
	
	species boids skills: [moving] {
		float speed max: maximal_speed <- maximal_speed;
		float range <- minimal_distance * 2;
		int heading max: heading + maximal_turn min: heading - maximal_turn;
		point velocity <- {0,0};
		int size <- 5;
		
		list<boids> others update: ((boids overlapping (circle (range)))  - self);
		
		point mass_center update:  (length(others) > 0) ? ((mean (others collect (each.location)) ) as point) : location;
		
		reflex separation when: apply_separation {
			point acc <- {0,0}; 
			loop boid over: (boids overlapping (circle(minimal_distance)))  {
				acc <- acc - ((location of boid) - location);
			}  
			velocity <- velocity + acc;
		}
		
		reflex alignment when: apply_alignment {
			point acc <- (mean (others collect (each.velocity)) as point) - velocity;
			velocity <- velocity + (acc / alignment_factor);
		}
		 
		reflex cohesion when: apply_cohesion {
			point acc <- mass_center - location;
			acc <- acc / cohesion_factor;
			velocity <- velocity + acc; 
		}
		
		reflex avoid when: apply_avoid {
			let acc <- {0,0};
			let nearby_obstacles <- (obstacle overlapping (circle (range)) );
			loop obs over: nearby_obstacles {
				acc <- acc - ((location of obs) - my (location));
			}
			velocity <- velocity + acc; 
		}
		
		action bounding {
			if  !(torus_environment) {
				if  (location.x) < xmin {
					velocity <- velocity + {bounds,0};
				} else if (location.x) > xmax {
					velocity <- velocity - {bounds,0};
				}
				
				if (location.y) < ymin {
					velocity <- velocity + {0,bounds};
				} else if (location.y) > ymax {
					velocity <- velocity - {0,bounds};
				}
				
			}
		}
		
		reflex follow_goal when: apply_goal {
			velocity <- velocity + ((goal - location) / cohesion_factor);
		}
		
		reflex wind when: apply_wind {
			velocity <- velocity + wind_vector;
		}
		  
		action do_move {  
			if (((velocity.x) as int) = 0) and (((velocity.y) as int) = 0) {
				velocity <- {(rnd(4)) -2, (rnd(4)) - 2};
			}
			point old_location <- location;
			do goto target: location + velocity;
			velocity <- location - old_location;
		}
		
		reflex movement {
			do bounding;
			do do_move;
		}
		
		aspect basic{
			draw triangle(boids_size) color:rgb('black');
		}
		aspect image {
			draw (images at (rnd(2))) size: boids_size rotate: heading color: rgb('black') ;			    
		}
				
		aspect dynamicColor{
			float hue <- heading/360;
			rgb cc <- hsb (hue,1.0,1.0);
			draw triangle(20) size: 15 rotate: 90 + heading color: cc border:cc depth:5;
			draw name;
		}
	} 
	
	species obstacle skills: [moving] {
		float speed <- 0.1;	 
		aspect default {
			draw triangle(20) color: rgb('yellow') depth:5;
		}
	}
}


experiment start type: gui {
	output {
		display RealBoids  type:opengl ambient_light:255 z_fighting:false{
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: dynamicColor transparency:0.5 position:{0,0,0.1};
			species boids aspect: image transparency:0.5 position:{0,0,0.11};
			species boids_goal transparency:0.2 position:{0,0,0.1};
			species obstacle position:{0,0,0.1}; 		
		}
	}
}

experiment trajectory_analysis type: gui {
	output {
		
		display RealBoids  type:opengl ambient_light:255 z_fighting:false{
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: dynamicColor transparency:0.5 position:{0,0,0.1};
			species boids aspect: image transparency:0.5 position:{0,0,0.11};
			species boids_goal transparency:0.2 position:{0,0,0.1};
			species obstacle position:{0,0,0.1}; 		
		}
		
		
		display RealBoidsAggregatedBoids  type:opengl trace:true{
			image name:'background' file:'../images/ocean.jpg';
			species aggregatedboids  aspect: base position:{0,0,0.1+time/200};
			species boids_goal aspect:sphere position:{0,0,0.1+time/200};		
		}
	}
}

experiment aggregated type: gui {
	output {
		display RealBoids  type:opengl ambient_light:255 z_fighting:false{
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: dynamicColor transparency:0.5 position:{0,0,0.1};
			species boids aspect: image transparency:0.5 position:{0,0,0.11};
			species boids_goal transparency:0.2 position:{0,0,0.1};
			species obstacle position:{0,0,0.1}; 		
		}
		
		
		display RealBoidsAggregatedTrajectory  type:opengl trace:true{
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: dynamicColor transparency:0.5 position:{0,0,0.1};
			species boids_goal aspect:sphere transparency:0.2 position:{0,0,0.1};
			species obstacle position:{0,0,0.1}; 		
		}
		
		display RealBoidsSpaceTimeCube  type:opengl ambient_light:255 z_fighting:false trace:true{
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: dynamicColor transparency:0.5 position:{0,0,0.1+time/100};
			species boids_goal aspect:sphere transparency:0.2 position:{0,0,0.1+time/100};
			species obstacle position:{0,0,0.1}; 		
		}
	}
}

experiment MultipleView type: gui {
	output {


		display RealBoids   type:opengl ambient_light:255 {
			image name:'background' file:'../images/ocean.jpg';
			species boids aspect: image  transparency:0.5 position:{0,0,0.25};
			species boids_goal transparency:0.2 position:{0,0,0.25};
			species obstacle ;
			species boids  aspect: dynamicColor transparency:0.2 position:{0,0,0.24};		
		}
		
		display ThirdPersonn  type:opengl camera_pos:{int(first(boids).location.x),-int(first(boids).location.y),250} 
		camera_look_pos:{int(first(boids).location.x),-(first(boids).location.y),0} {
		
			image name:'background' file:'../images/ocean.jpg';
			species obstacle;
			species boids  aspect: dynamicColor transparency:0.2 ;
			species boids_goal  transparency:0.2; 		
		}
		
			
		display FirstPerson  type:opengl ambient_light:100 camera_pos:{int(first(boids).location.x),-int(first(boids).location.y),10} 
		camera_look_pos:{cos(first(boids).heading)*world.shape.width,-sin(first(boids).heading)*world.shape.height,0} camera_up_vector:{0.0,0.0,1.0} {	
			image name:'background' file:'../images/ocean.jpg';
			species obstacle ;
			species boids  aspect: dynamicColor transparency:0.2 ;
			species boids_goal  transparency:0.2; 		
		}
	}
}
