// This file was automatically generated from closure_templates.soy.
// Please don't edit this file by hand.

if (typeof spdv == 'undefined') { var spdv = {}; }
if (typeof spdv.templates == 'undefined') { spdv.templates = {}; }


spdv.templates.helloWorld = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('Hello world, from Closure Template!');
  if (!opt_sb) return output.toString();
};


spdv.templates.helloName = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append((! opt_data.greetingWord) ? 'Hello ' + soy.$$escapeHtml(opt_data.name) + '!' : soy.$$escapeHtml(opt_data.greetingWord) + ' ' + soy.$$escapeHtml(opt_data.name) + '!');
  if (!opt_sb) return output.toString();
};


spdv.templates.helloNames = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  spdv.templates.helloName(opt_data, output);
  output.append('<br>');
  var additionalNameList18 = opt_data.additionalNames;
  var additionalNameListLen18 = additionalNameList18.length;
  if (additionalNameListLen18 > 0) {
    for (var additionalNameIndex18 = 0; additionalNameIndex18 < additionalNameListLen18; additionalNameIndex18++) {
      var additionalNameData18 = additionalNameList18[additionalNameIndex18];
      spdv.templates.helloName({name: additionalNameData18}, output);
      output.append((! (additionalNameIndex18 == additionalNameListLen18 - 1)) ? '<br>' : '');
    }
  } else {
    output.append('No additional people to greet.');
  }
  if (!opt_sb) return output.toString();
};
