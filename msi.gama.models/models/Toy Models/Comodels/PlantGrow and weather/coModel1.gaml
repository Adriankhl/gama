/**
* Name: Comodel0 
* Author: Benoit Gaudou & Damien Philippon
* Description: Second co-Model
*  - instanciate a sub-model in a model
*  - step it
*  - display its agents
*  - compute indicators on it 
* Tags: comodel
*/

model coModel

import "weather.gaml" as weather


global {
	
	weather weather_simu ;
		
	init {
		create weather.weather_coModeling with: [grid_size::30,write_in_console_step::false];
		weather_simu <- first(weather.weather_coModeling).simulation; 
	}

	reflex simulate_micro_models
	{
		ask weather_simu
		{
			do _step_;
		}
	}
}

experiment coModel type: gui {
	output {
		display d {
			agents "weather" value: weather_simu.plotWeather ;
		}
		
		display data {
			chart "rain" type: series {
				data "rainfall" value: sum(weather_simu.plotWeather accumulate (each.rain));
			}
		}
		
	}
}