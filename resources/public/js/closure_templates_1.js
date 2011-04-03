// This file was automatically generated from closure_templates.soy.
// Please don't edit this file by hand.

if (typeof spdv == 'undefined') { var spdv = {}; }
if (typeof spdv.templates == 'undefined') { spdv.templates = {}; }


spdv.templates.statusElementOther = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<instance>', soy.$$escapeHtml(opt_data.instanceId), ' ', soy.$$escapeHtml(opt_data.memberHost), ' ', soy.$$escapeHtml(opt_data.memberName), '</instance>');
  if (!opt_sb) return output.toString();
};


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
  var additionalNameList26 = opt_data.additionalNames;
  var additionalNameListLen26 = additionalNameList26.length;
  if (additionalNameListLen26 > 0) {
    for (var additionalNameIndex26 = 0; additionalNameIndex26 < additionalNameListLen26; additionalNameIndex26++) {
      var additionalNameData26 = additionalNameList26[additionalNameIndex26];
      spdv.templates.helloName({name: additionalNameData26}, output);
      output.append((! (additionalNameIndex26 == additionalNameListLen26 - 1)) ? '<br>' : '');
    }
  } else {
    output.append('No additional people to greet.');
  }
  if (!opt_sb) return output.toString();
};
