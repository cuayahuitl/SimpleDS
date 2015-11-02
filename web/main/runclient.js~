/********************************************************************
 * Implementation of a client DRL agent with the following features:
 * + It connects to a Java-based WebServer  
 * + It learns non-linear policies via ConvNetJS
 * + It saves and loads policies for testing
 * + It supports full and constrained action spaces
 * + It generates information for plotting learning curves
 * <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 *******************************************************************/

var WebSocket = require('../node_modules/ws/lib/WebSocket');
var DeepQLearn = require('../convnet/deepqlearn');
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

var num_inputs = parseInt(config["NumInputs"]); 
var num_actions = parseInt(config["NumActions"])+1;
var temporal_window = 1; 
var network_size = num_inputs*temporal_window + num_actions*temporal_window + num_inputs;

var layer_defs = [];
layer_defs.push({type:'input', out_sx:1, out_sy:1, out_depth:network_size});
layer_defs.push({type:'fc', num_neurons: 40, activation:'relu'});
layer_defs.push({type:'fc', num_neurons: 40, activation:'relu'});
layer_defs.push({type:'regression', num_neurons:num_actions});

var tdtrainer_options = {learning_rate:0.001, momentum:0.0, batch_size:parseInt(config["BatchSize"]), l2_decay:0.01};

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

var app = {};
app.address_port = config["AddressPort"];
app.output_path = "results";
app.training_policy = app.output_path+"/simpleds-policy.json";
app.training_output = app.output_path+"/simpleds-output.txt";
app.test_policy = app.output_path+"/simpleds-policy.json";
app.freq_policy_saving = parseInt(config["SavingFrequency"]);

console.log("app.training_policy="+app.training_policy);
console.log("app.training_output="+app.training_output);
console.log("app.test_policy="+app.test_policy);

console.log("configurations defined!");

var brain = new DeepQLearn.Brain(num_inputs, num_actions, opt);

console.log("brain created!");

var ws = new WebSocket(app.address_port);

ws.onopen = function() {
	ws.send("Hello Server -- from JavaScript");
	console.log("Connected to server!");
	init();
	run();
};

ws.onmessage = function(evt) {
	if(world == null) { 
		alert("World is not created yet!");
		ws.close()
		
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
			this.features = [];
			this.observedActions = [];
			this.observedRewards = [];

			var list = rawFeatures.split("|");
			for(var i=0;i<list.length;i++) {
				var pair = list[i].split("=");
				var param = pair[0];

				if (param == "state") {
					var vector = pair[1].split(",");
					for(var j=0;j<vector.length;j++) {
						this.features[j] = vector[j];
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

			if (this.features.length != num_inputs) {
				console.log("WARNING: The amount of features doesn't match num_inputs="+num_inputs);
				console.log("this.features="+this.features + " size="+this.features.length);
				process.exit(1);
			}
			
			this.agents[0].set_actions(this.observedActions);
			this.agents[0].forward(this.features);
			this.agents[0].backward(this.observedActions, this.observedRewards);

			ws.send("action="+action);
		},
		save: function() {
			if (this.clock%100 === 0 && start === true) {
				var average_reward = this.agents[0].brain.average_reward_window.get_average();
				var epsilon = this.agents[0].brain.epsilon;
				var average_actions = this.totalActions / this.clock;
                                var currentTime = new Date().getTime();
                                var learningTime = (currentTime - world.startTime)/(1000*60*60);
				var output_tuple = this.clock +" "+ average_reward.toFixed(4);
				output_tuple += " "+ epsilon.toFixed(4) + " "+ average_actions.toFixed(2);
				output_tuple += " "+ this.totalDialogues +" "+learningTime.toFixed(4);
				world.data += output_tuple + "\n";
				console.log(output_tuple);
			}
			if (this.clock%app.freq_policy_saving === 0 && this.agents[0].brain.learning) {
				save_output(app.training_output);
				save_policy(app.training_policy);
			}
		}
}

var Agent = function() {
	this.brain = brain;
	this.actions = [];
	for(var i=0; i<this.brain.num_actions-1; i++) {
		this.actions.push(""+i);
	}
}

Agent.prototype = {
		forward: function(input_array) {
			this.prevAction = action;
			action = this.brain.forward(input_array);
		},
		backward: function(observedActions, observedRewards) { 
			var numreward = parseFloat(observedRewards[action]);
			this.brain.backward(numreward);
		},
		set_actions: function(array) { 
			this.brain.allowed_actions_array = array;
		}
}

function save_policy(filename) {
	var j = world.agents[0].brain.value_net.toJSON();
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
		var pair = list[i].split("=");
		config[pair[0]] = pair[1];
	}
}

function load_policy(filename) {
	var file2Read = "../../"+filename;
	var text = require(file2Read);
	var j = JSON.parse(text);
	world.agents[0].brain.value_net.fromJSON(j); 
	console.log("Network initialised!");
}

function run() {
	if (process.argv.length < 3) {
		console.log('Usage: node ' + process.argv[1] + ' [train|test]');
		process.exit(1);
	}

	var execmode = process.argv[2];

	if (execmode == "train") {
		world.agents[0].brain.learning = true;
		console.log("Running in training mode!");
		console.log("[steps avg.reward epsilon avg.actions dialogues time.hrs]");
		start = true;

	} else if (execmode == "test") {
		load_policy(app.test_policy);
		world.agents[0].brain.learning = false;
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
