model prey_predator
//Model 2 of the predator/prey tutorial

global {
	var nb_preys_init type: int init: 200 min: 1 max: 1000 parameter: 'Initial number of preys: ' category: 'Prey' ;
	init {
		create species: prey number: nb_preys_init ;
	}
}
entities {
	species prey {
		const size type: float init: 2 ;
		const color type: rgb init: 'blue' ;
		var myCell type: vegetation_cell init: one_of (vegetation_cell as list) ;
		init {
			set location value: myCell.location;
		}
		aspect base {
			draw shape: circle size: size color: color ;
		}
	}
}
environment width: 100 height: 100 {
	grid vegetation_cell width: 50 height: 50 neighbours: 4 {
		const maxFood type: float init: 1.0 ;
		const foodProd type: float init: (rnd(1000) / 1000) * 0.01 ;
		var food type: float init: (rnd(1000) / 1000) value: min [maxFood, food + foodProd] ;
		var color type: rgb value: [255 * (1 - food), 255, 255 * (1 - food)] init: [255 * (1 - food), 255, 255 * (1 - food)] ;
	}
}
output {
	display main_display {
		grid vegetation_cell lines: 'black' ;
		species prey aspect: base ;
	}
}
