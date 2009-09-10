/****************************************************************************
 * Compiere (c) Jorg Janke - All rights reseverd
 * $Id: wstore.js,v 1.5 2006/05/23 22:39:03 mdeaelfweald Exp $
 *
 * Preload Image Script
 ***************************************************************************/
 
var myimages = new Array();
function preloading()
{
	for (i=0; i<preloading.arguments.length; i++)
	{
		myimages[i] = new Image();
		myimages[i].src = preloading.arguments[i];
	}
}
preloading('../images/ConversantFade.jpg','../images/ConversantHeaderBG.jpg','../images/conversant_logo_FW_55_flat.png');