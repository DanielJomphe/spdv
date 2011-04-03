// This file was automatically generated from closure_templates.soy.
// Please don't edit this file by hand.

if (typeof spdv == 'undefined') { var spdv = {}; }
if (typeof spdv.templates == 'undefined') { spdv.templates = {}; }


spdv.templates.statusInstanceSelf = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<instance class="self"><form action="/" method="POST"><input id="_method" name="_method" type="hidden" value="PUT"><label for="new-name">', soy.$$escapeHtml(opt_data.instanceId), ' ', soy.$$escapeHtml(opt_data.memberHost), '</label><input  id="new-name" name="new-name" type="text"   value="', soy.$$escapeHtml(opt_data.memberName), '"><input  id="cur-name" name="cur-name" type="hidden" value="', soy.$$escapeHtml(opt_data.memberName), '"><input type="submit" value="Changer le nom"></form></instance>');
  if (!opt_sb) return output.toString();
};


spdv.templates.statusInstanceOther = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<instance class="other">', soy.$$escapeHtml(opt_data.instanceId), ' ', soy.$$escapeHtml(opt_data.memberHost), ' ', soy.$$escapeHtml(opt_data.memberName), '</instance>');
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
  var additionalNameList36 = opt_data.additionalNames;
  var additionalNameListLen36 = additionalNameList36.length;
  if (additionalNameListLen36 > 0) {
    for (var additionalNameIndex36 = 0; additionalNameIndex36 < additionalNameListLen36; additionalNameIndex36++) {
      var additionalNameData36 = additionalNameList36[additionalNameIndex36];
      spdv.templates.helloName({name: additionalNameData36}, output);
      output.append((! (additionalNameIndex36 == additionalNameListLen36 - 1)) ? '<br>' : '');
    }
  } else {
    output.append('No additional people to greet.');
  }
  if (!opt_sb) return output.toString();
};
