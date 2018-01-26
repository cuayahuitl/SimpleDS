/********************************************************************
 * Implementation of a client DRL agent with the following features:
 * + It communicates with a Java-based WebServer  
 * + It learns non-linear policies via ConvNetJS
 * + It saves and loads image-based supervised learners
 * + It generates information for plotting learning curves
 * <ahref="mailto:couly.guillaume@gmail.com">Guillaume Couly</a>
 * <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 *******************************************************************/

var WebSocket = require('../node_modules/ws/lib/WebSocket');
var DeepQLearn = require('../convnet/deepqlearn');
var convnetjs = require('../convnet/convnet');
var exec = require('child_process').exec, child;
var fs = require('fs');

console.log("libraries loaded!");	

var config = {};
require.extensions['.txt'] = function (module, filename) {
    module.exports = fs.readFileSync(filename, 'utf8');
};
var configpipe = require('../../config.txt');
parse_configfile(configpipe);

console.log("config file loaded!");

var ws = new WebSocket(config["AddressPort"]);
var learning = true 
console.log("web socket created!");

var num_inputs_cols = null; 
var num_inputs_rows = null; 
var num_actions = null;
var temporal_window = 1; 
var network_size = null;
var trainer = null;
var layer_defs = [];
var net = null;
var rew = 0.0; 
var numPredictions = 0.0;
var numBurningInstances = 10;
var tdtrainer_options = {learning_rate:0.001, momentum:0.0, batch_size:parseInt(config["BatchSize"]), l2_decay:0.01};
var first = true

var opt = {};
opt.temporal_window = temporal_window;
opt.experience_size = parseInt(config["ExperienceSize"]);
opt.start_learn_threshold = parseInt(config["BurningSteps"]);
opt.gamma = parseFloat(config["DiscountFactor"]);
opt.learning_steps_total = parseInt(config["LearningSteps"]);
opt.learning_steps_burnin = parseInt(config["BurningSteps"]);
opt.epsilon_min = parseFloat(config["MinimumEpsilon"]);
opt.epsilon_test_time = 0.00;
opt.layer_defs = layer_defs;
opt.tdtrainer_options = tdtrainer_options;

var output_path = config["OutputPath"];
var training_policy = output_path+"/pixels/simpleds-policy.json";
var training_output = output_path+"/pixels/simpleds-output.txt";
var test_policy = output_path+"/pixels/simpleds-policy.json";
var freq_policy_saving = parseInt(config["SavingFrequencyPixels"]);

console.log("configurations defined!");


ws.onopen = function() {
	ws.send("Hello Server -- from JavaScript");
	console.log("Connected to server!");
};

ws.onmessage = function(evt) {
	if(world == null) {
		var input_outputs = evt.data.split(",");
		num_inputs_cols = parseInt(input_outputs[0]);
		num_inputs_rows = parseInt(input_outputs[1]);
		num_actions = parseInt(input_outputs[2]);
		createBrain();
		init();
		run();
		
	} else {
		world.tick(evt.data);
		world.save();
	}
};

ws.onclose = function() {
	console.log("Connexion closed!");
};

ws.onerror = function(err) {
	console.log("Error: " + err);
};


var world, start, action;

var World = function() {
	this.agents = [];
	this.data = "";
	this.clock = 0;
	this.totalActions = 0;
	this.totalDialogues = 0;
        this.startTime = new Date().getTime();
}

World.prototype = {      
		tick: function(rawFeatures) {
			this.clock++;
			this.features = new convnetjs.Vol(num_inputs_rows,num_inputs_cols,1,0.0) ; 
			this.observedActions = [];
			this.observedRewards = [];
			
			var list = rawFeatures.split("|");
			for(var i=0;i<list.length;i++) {
				var pair = list[i].split("=");
				var param = pair[0];

				if (param == "state") {
					var vector = pair[1].split(",");
					
					var counter = 0
					for(var j=0;j<num_inputs_rows;j++) {
						for(var k=0;k<num_inputs_cols;k++) {
							//var pixelval = vector[counter]>0 ? 1 : 0;
							//var pixelval = parseInt(vector[counter])+128;
							var pixelval = vector[counter]>0 ? vector[counter]/128 : 0;
							this.features.set(j, k, 0, pixelval);
							counter += 1;
						}
					}

				} else if (param === "actions") {
					var vector = pair[1].split(",");
					for(var j=0;j<vector.length;j++) {
						this.observedActions[j] = vector[j];
						this.totalActions++;
					}

				} else if (param === "rewards") {
					var vector = pair[1].split(",");
					for(var j=0;j<vector.length;j++) {
						this.observedRewards[j] = vector[j];
					}

				} else if (param === "dialogues") {
					this.totalDialogues = parseFloat(pair[1]);
					
				} else{
					console.log("WARNING: Unknown param="+param);
					process.exit(1);
				}
			}
			
			if ( first && learning || !learning){
				first = false ;
				
				for(var j=0;j<num_inputs_rows;j++) {
					str = "" ;
					for(var k=0;k<num_inputs_cols;k++) {
						str = str + " " + this.features.get(j , k , 0);
					}
					console.log(str)
				}
			}
						
			this.agents[0].set_actions(this.observedActions);
			if (learning && this.clock>numBurningInstances){
				var label=null;
				for (var i=0; i<this.observedRewards.length; i++) {
					if (this.observedRewards[i] == 1) {
						label=i;
						break;
					}
				}

				res = this.agents[0].trainer.train(this.features , label);
				net.forward( this.features ) ;
				action = net.getPrediction();
				
				if ( action == this.observedActions[0] ){
					rew += 1.0
				}
  
				numPredictions += 1 ;

			} else {
				var scores = net.forward( this.features ) ;
				action = net.getPrediction();
				var sum = 0
				for (var i=0; i<scores["w"].length; i++) {
					sum += Math.exp(parseFloat(scores["w"][i]));
				}
				//console.log("sum="+sum);
				var dist = {}
				for (var i=0; i<scores["w"].length; i++) {
					//console.log("i="+i +" action="+action + " len="+(scores["w"].length)); 
					if (parseInt(action) == i) {
						var prob = Math.exp(parseFloat(scores["w"][i]))/sum;
						action = action+":"+prob;
						//console.log("i="+i + " prob="+prob);
						break;
					}
				}
				console.log( "a : " + action);
			}
			
			ws.send("action="+action);
		},
		save: function() {
			if (this.clock%100 === 0 && start === true) {
				var average_reward = rew/numPredictions ; 
				rew = 0 ;
				numPredictions = 0 ;
				var average_actions = this.totalActions / this.clock;
                		var currentTime = new Date().getTime();
               			var learningTime = (currentTime - world.startTime)/(1000*60*60);
				var output_tuple = this.clock +" "+ average_reward.toFixed(4);
				output_tuple += " 0.00 "+ average_actions.toFixed(2);
				output_tuple += " "+ this.totalDialogues +" "+learningTime.toFixed(4);
				world.data += output_tuple + "\n";
				console.log(output_tuple);
			}
			if (this.clock%freq_policy_saving === 0 && learning) {
				save_output(training_output);
				save_policy(training_policy);
			}
		}
}

var Agent = function() {
	this.trainer = trainer;
	this.actions = [];
	for(var i=0; i<this.trainer.num_actions-1; i++) {
		this.actions.push(""+i);
	}
}

Agent.prototype = {
		forward: function(input_array) {
			this.prevAction = action;
			action = this.trainer.forward(input_array);
		},	
		backward: function(observedActions, observedRewards) { 
			var numreward = parseFloat(observedRewards[action]);
			this.trainer.backward(numreward);
		},
		set_actions: function(array) { 
			this.trainer.allowed_actions_array = array;
		}
}

function save_policy(filename) {
	var j = net.toJSON();
	var text = JSON.stringify(j);
	var file2Save = "../../"+filename;
	fs.writeFile(file2Save, JSON.stringify(text, null, 4), function(err) {
		if(err) throw err;
		console.log("Saved policy "+file2Save);
	});
}

function save_output(filename) {
	var file2Save = "../../"+filename;
	fs.writeFile(file2Save, world.data, function(err) {
		if(err) throw err;
		console.log("Saved output "+file2Save);
	});
}

function parse_configfile(data) {
	var list = data.split("\n");
	for(var i=0;i<list.length;i++) {
		var line = list[i].trim();
		if (!line || !line.length) {
			continue;
		} else {
			var pair = line.split("=");
			config[pair[0]] = pair[1].toString();
		}
	}
}

function load_policy(filename) {
	var file2Read = "../../"+filename;
	console.log( file2Read )
	var text = require(file2Read);
	var j = JSON.parse(text);
	net.fromJSON(j);

	console.log("Network initialised!");
}

function run() {
	if (process.argv.length < 3) {
		console.log('Usage: node ' + process.argv[1] + ' (train|test) [num_dialogues] [-v|-nv]');
		process.exit(1);
	}

	var execmode = process.argv[2];
	ws.send("params="+process.argv);

	if (execmode == "train") {
		learning = true ;
		console.log("Running in training mode!");
		console.log("[steps avg.reward epsilon avg.actions dialogues time.hrs]");
		start = true;

	} else if (execmode == "test") {
		load_policy(test_policy);
		learning = false ;
		console.log("Running in test mode!");
		console.log("[steps avg.reward epsilon avg.actions dialogues time.hrs]");
		start = true;

	} else {
		console.log("UNKNOWN execution mode!");        
		process.exit(1);
	}
}

function init() {
	start = false;
	world = new World();
	world.agents = [new Agent()];

	console.log("Initialised agent!");
}

function createBrain() {
	console.log ( "Inputs : " + num_inputs_cols + "x" + num_inputs_rows )
	console.log ( "Outputs : " + num_actions )
	
	layer_defs.push({type:'input', out_sx:num_inputs_cols, out_sy:num_inputs_rows, out_depth:1});
	layer_defs.push({type:'conv', sx:5, filters:8, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'conv', sx:5, filters:16, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:3, stride:3});
	layer_defs.push({type:'softmax', num_classes:num_actions});
	
	net = new convnetjs.Net();
	net.makeLayers(layer_defs);

	if (learning){
		trainer = new convnetjs.SGDTrainer(net, {method:'adadelta', batch_size:1, l2_decay:0.001});
	}

	console.log("brain created!");
}
