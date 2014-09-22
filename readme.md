SpritePlacer

an editor for arbitrarily placing a collection of sprites - loads and saves to xml.

(see below for longer description)

--- beginning of licence ---

(C) Mr C Camacho 2013 - all rights reserved

I hereby grant free use of this work, providing you agree to abide by the following:

This notice and credit for the original source must be included with any redistribution.

All changes to the source should be clearly marked and all sources must be made available with any redistribution, including preprepared "binary" redistributions of the work.

Unless required by applicable law or agreed to in writing, Licensor provides this work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing this work and assume any risks associated with Your exercise of permissions under this License.

--- end of licence ---

Allows the following manipulations on individual sprites

* Name – a textual name that can be used for a sprite, has no functional use in the editor, could be used for user defined data...
* Xpos Ypos – the coordinates the centre of the sprite is placed.
* width height – the size of the sprite also used in conjunction with scale and wrap for texture effects
* angle – the rotation of the sprite (in degrees)
* offsetX offsetY – texture offset in pixels
* scaleX scaleY – scales the size of the sprite
* texture – the file name of the texture to use.
* Xwrap Ywrap – effects how the texture wraps if it is not big enough to cover the sprite.
* various physics properties (multiple physics shapes per sprite)

The current sprite is selected with the mouse (or touch) 

In addition to manipulating individual sprites where the camera is looking can be moved by "dragging"

There is a (somewhat rudimentry) file dialog to assist with loading and saving "levels"

New sprites can be added with the “add” button, they can be deleted with the remove button once selected

TODO

* more physics properties
* allow deletion of physics shapes
* add scrpting
* add button to create box office smash game.

I'm open to sensible suggestions, but do contact me before diving into massive code changes, as depending on what I'm doing next I might not be able to use them...

hopefully spambots may have trouble with this...

bedroomcoders co uk

codifies with
