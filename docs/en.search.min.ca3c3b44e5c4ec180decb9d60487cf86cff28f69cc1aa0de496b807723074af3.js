'use strict';(function(){const input=document.querySelector('#book-search-input');const results=document.querySelector('#book-search-results');if(!input){return}
input.addEventListener('focus',init);input.addEventListener('keyup',search);document.addEventListener('keypress',focusSearchFieldOnKeyPress);function focusSearchFieldOnKeyPress(event){if(input===document.activeElement){return;}
const characterPressed=String.fromCharCode(event.charCode);if(!isHotkey(characterPressed)){return;}
input.focus();event.preventDefault();}
function isHotkey(character){const dataHotkeys=input.getAttribute('data-hotkeys')||'';return dataHotkeys.indexOf(character)>=0;}
function init(){input.removeEventListener('focus',init);input.required=true;loadScript('/keycloakmigration/flexsearch.min.js');loadScript('/keycloakmigration/en.search-data.min.2cfc6d1564cbec07a17c328322e30d57bb14c20bf3eb74fe5e5a5e0c1fc55616.js',function(){input.required=false;search();});}
function search(){while(results.firstChild){results.removeChild(results.firstChild);}
if(!input.value){return;}
const searchHits=window.bookSearchIndex.search(input.value,10);searchHits.forEach(function(page){const li=document.createElement('li'),a=li.appendChild(document.createElement('a'));a.href=page.href;a.textContent=page.title;results.appendChild(li);});}
function loadScript(src,callback){const script=document.createElement('script');script.defer=true;script.async=false;script.src=src;script.onload=callback;document.head.appendChild(script);}})();