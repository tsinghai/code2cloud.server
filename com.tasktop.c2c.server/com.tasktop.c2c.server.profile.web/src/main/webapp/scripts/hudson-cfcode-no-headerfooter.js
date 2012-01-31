/*
 * GLOBAL
 */

 var url = decodeURIComponent(window.location.toString());
 var appRoot = url.split('/s/')[0];
 var appId = url.split('/s/')[1].split('/')[0];

 var HUDSONVARS = [];
 HUDSONVARS['show-admin-option'] = false;
 HUDSONVARS['url-parts'] = url.split('://')[1].split('/');
 HUDSONVARS['project-nav'] = [];
 HUDSONVARS['project-nav']['home'] = appRoot + '/#projects/' + appId;
 HUDSONVARS['project-nav']['dashboard'] = appRoot + '/#projects/' + appId + '/dashboard';
 HUDSONVARS['project-nav']['tasks'] = appRoot + '/#projects/' + appId + '/tasks/recent';
 HUDSONVARS['project-nav']['builds'] = appRoot + '/s/' + appId + '/hudson/';
 HUDSONVARS['project-nav']['deployments'] = appRoot + '/#projects/' + appId + '/deployments';
 HUDSONVARS['project-nav']['team'] = appRoot + '/#projects/' + appId + '/team';
 HUDSONVARS['project-nav']['docs'] = appRoot + '/#projects/' + appId + '/wiki';

/*
 *Set up some basic functions: See documentation here: http://code.google.com/p/vxjs/wiki/vxCore
 */

var _=_?_:{}
_.entity=_.H=function(s,d,t){t=_.d.createElement('textarea');t.innerHTML=s;return d?t.value:t.innerHTML}
_.d=document
_.id=_.G=function(e){return e.style?e:_.d.getElementById(e)}
_.addclass=_.AC=function(e,c){if(_.HC(e))e.className+=' '+c}
_.index=_.I=function(v,a,i){for(i=a.length;i--&&a[i]!=v;);return i}
_.hasclass=_.HC=function(e,c){return _.I(c,e.className.split(" "))>0}
_.cls=_.C=function(n,d){if(!d) d = document.getElementsByTagName("body")[0];var a = [];var re = new RegExp('\\b' + n + '\\b');var els = d.getElementsByTagName("*");for(var i=0,j=els.length; i<j; i++)if(re.test(els[i].className))a.push(els[i]);if(a.length == 1)return a[0];return a;}
_.ready=_.R=function(f){"\v"=="v"?setTimeout(f,0):_.E(_.d,'DOMContentLoaded',f)}
_.on=_.E=function(e,t,f,r){if(e.attachEvent?(r?e.detachEvent('on'+t,e[t+f]):1):(r?e.removeEventListener(t,f,0):e.addEventListener(t,f,0))){e['e'+t+f]=f;e[t+f]=function(){e['e'+t+f](window.event)};e.attachEvent('on'+t,e[t+f])}}
_.clone=_.O=function(j,c){if(c)return _.S(_.S(j),1);function p(){};p.prototype=j;return new p()}
_.json=_.S=function(j,d,t){if(d)return eval('('+j+')');if(!j)return j+'';t=[];if(j.pop){for(x in j)t.push(_.S(j[x]));j='['+t.join(',')+']'}else if(typeof j=='object'){for(x in j)t.push(x+':'+_.S(j[x]));j='{'+t.join(',')+'}'}else if(j.split)j="'"+j.replace(/\'/g,"\\'")+"'";return j}
_.toggle=_.T=function(e){(e.style.display=='none')?e.style.display='block':e.style.display='none';}
_.close=_.CL=function(e){e.style.display='none';}

var pageHTML = '<div class="container" id="container"> '+
			' <div class="auto-refresh right"><a href="?auto_refresh=true">ENABLE AUTO REFRESH</a></div></div>';

var newHeader = _.d.createElement('div');
newHeader.className = "site";
newHeader.innerHTML = pageHTML;

var header = _.G('header');
header.parentNode.insertBefore(newHeader, header);
header.parentNode.removeChild(header);
var footer = _.G('footer').parentNode.parentNode.parentNode;
footer.parentNode.removeChild(footer);

/*
 * Initial Setup Complete - Start tweaking from here
 */
 
setTimeout(function() {
  /* Let the new DOM Settle */

  /* Move the Main Table */
  var mainTable = _.G('main-table');
  mainTable.removeAttribute('style');
  var container = _.G('container');
  container.appendChild(mainTable);
  
  /* Now that we're done, remove the style which was hiding the body */
  var headElem = document.getElementsByTagName('head')[0];
  var styleElems = headElem.getElementsByTagName('style');
  for(i = 0; i < styleElems.length; i++) {
    var curStyle = styleElems[i];
    if(curStyle.title == 'loading-CSS')
      headElem.removeChild(curStyle);
  }
}, 50);