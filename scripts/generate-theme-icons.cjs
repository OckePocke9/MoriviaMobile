// Reproducible native app-icon palette assets from Morivia's existing ribbon silhouette.
// Run with NODE_PATH pointing to an installation of sharp. No service/network is used.
const sharp = require('sharp');
const fs = require('node:fs/promises');
const path = require('node:path');
const root = process.argv[2] || process.cwd();
const palettes = {
 original: null, arctic_blue:['#A8DCFF','#4C9DFF','#2D56A8'], emerald:['#21C45A','#16A34A'],
 rose_gold:['#F6D5D8','#DE9FA6','#8C4E5A'], copper:['#FFD6A4','#C7793A','#80421D'], graphite:['#F3F5F7','#AAB2BE','#687381'],
 gold:['#8A5700','#E8A91C','#FFF1A8','#FFD45C','#9A6200'], jade:['#A8F0CD','#22D37C','#0B8F59'],
 crimson:['#E53935','#C62828'], ocean:['#2196F3','#1565C0'], violet:['#AB47BC','#7B1FA2'],
 amber:['#FFB300','#FF8F00'], rose:['#EC407A','#AD1457'], white:['#FFFFFF','#D0D0D0'],
};
const platforms={original:'AppIcon',arctic_blue:'AppIconArcticBlue',emerald:'AppIconEmerald',rose_gold:'AppIconRoseGold',copper:'AppIconCopper',graphite:'AppIconGraphite',gold:'AppIconGold',jade:'AppIconJade',crimson:'AppIconCrimson',ocean:'AppIconOcean',violet:'AppIconViolet',amber:'AppIconAmber',rose:'AppIconRose',white:'AppIconWhite'};
async function put(p,b){p=path.join(root,p);await fs.mkdir(path.dirname(p),{recursive:true});await fs.writeFile(p,b);}
(async()=>{
 const themeSource=await fs.readFile(path.join(root,'composeApp/src/commonMain/kotlin/com/nuvio/app/core/ui/ThemeColors.kt'),'utf8');
 const names={gold:'Gold',jade:'Jade',rose_gold:'RoseGold',arctic_blue:'ArcticBlue',graphite:'Graphite',crimson:'Crimson',ocean:'Ocean',violet:'Violet',emerald:'Emerald',amber:'Amber',rose:'Rose',white:'White'};
 for(const [key,name] of Object.entries(names)) {
  const block=themeSource.split(`val ${name} = ThemeColorPalette(`)[1].split('\n    val ')[0];
  const gradient=block.match(/accentGradient = listOf\(([\s\S]*?)\)/);
  // Parse every Color() in the explicit gradient, otherwise the two accent colours.
  const gradientBlock=block.includes('accentGradient = listOf(')?block.split('accentGradient = listOf(')[1].split('nativeAccentHex')[0]:null;
  const colors=gradientBlock?[...gradientBlock.matchAll(/0xFF([0-9A-Fa-f]{6})/g)].map(m=>'#'+m[1]):[];
  const secondary=block.match(/secondary = Color\(0xFF([0-9A-Fa-f]{6})\)/);
  const variant=block.match(/secondaryVariant = Color\(0xFF([0-9A-Fa-f]{6})\)/);
  palettes[key]=colors.length>1?colors:['#'+secondary[1],'#'+variant[1]];
 }
 const bytes=await fs.readFile(path.join(root,'assets/branding/morivia-icon-source.png'));
 const {data,info}=await sharp(bytes).ensureAlpha().raw().toBuffer({resolveWithObject:true});
 let minX=info.width,minY=info.height,maxX=0,maxY=0;
 for(let y=0;y<info.height;y++)for(let x=0;x<info.width;x++)if(data[(y*info.width+x)*4+3]>=64){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}
 const w=maxX-minX+1,h=maxY-minY+1;
 const scale=360/Math.max(w,h), dx=256-(minX+w/2)*scale,dy=256-(minY+h/2)*scale;
 const image=`<image href="data:image/png;base64,${bytes.toString('base64')}" width="${info.width}" height="${info.height}" transform="translate(${dx} ${dy}) scale(${scale})"/>`;
 for(const [key,colors] of Object.entries(palettes)){
  const gradient=colors?`<linearGradient id="g" x1="0" y1="0" x2="1" y2="1">${colors.map((c,i)=>`<stop offset="${i/(colors.length-1)}" stop-color="${c}"/>`).join('')}</linearGradient>`:'';
  const ribbon=colors?`<defs>${gradient}<mask id="m" mask-type="alpha">${image}</mask></defs><rect width="512" height="512" fill="url(#g)" mask="url(#m)"/>`:image;
  const svg=(background,foreground=false)=>Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">${background?'<rect width="512" height="512" fill="black"/>':''}${foreground?'<g transform="translate(64 64) scale(0.75)">':''}${ribbon}${foreground?'</g>':''}</svg>`);
  const icon=await sharp(svg(true)).png().toBuffer();
  await put(`composeApp/src/commonMain/composeResources/drawable/app_icon_${key}.png`,icon);
  await put(`composeApp/src/commonMain/composeResources/drawable/morivia_symbol_${key}.png`,await sharp(svg(false)).png().toBuffer());
  await put(`composeApp/src/androidMain/res/drawable-nodpi/morivia_launcher_${key}.png`,icon);
  await put(`composeApp/src/androidMain/res/drawable-nodpi/morivia_foreground_${key}.png`,await sharp(svg(false,true)).png().toBuffer());
  const resource=key==='original'?'ic_launcher':`ic_launcher_${key}`;
  await put(`composeApp/src/androidMain/res/mipmap-anydpi/${resource}.xml`,`<layer-list xmlns:android="http://schemas.android.com/apk/res/android"><item android:drawable="@drawable/morivia_launcher_${key}" /></layer-list>\n`);
  await put(`composeApp/src/androidMain/res/mipmap-anydpi-v26/${resource}.xml`,`<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/ic_launcher_background"/><foreground android:drawable="@drawable/morivia_foreground_${key}"/></adaptive-icon>\n`);
  const dir=`iosApp/iosApp/Assets.xcassets/${platforms[key]}.appiconset`;
  await put(`${dir}/app-icon-1024.png`,await sharp(svg(true)).resize(1024,1024).png().toBuffer());
  await put(`${dir}/Contents.json`,JSON.stringify({images:[{filename:'app-icon-1024.png',idiom:'universal',platform:'ios',size:'1024x1024'}],info:{author:'xcode',version:1}},null,2)+'\n');
 }
 // Round icon and notification/splash default use the same centered artwork.
 await put('composeApp/src/androidMain/res/mipmap-anydpi/ic_launcher_round.xml','<layer-list xmlns:android="http://schemas.android.com/apk/res/android"><item android:drawable="@drawable/morivia_launcher_original" /></layer-list>\n');
 await put('composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml','<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/ic_launcher_background"/><foreground android:drawable="@drawable/morivia_foreground_original"/></adaptive-icon>\n');
 console.log('Generated 14 centered palettes for common UI, Android and iOS.');
})().catch(e=>{console.error(e);process.exitCode=1});
