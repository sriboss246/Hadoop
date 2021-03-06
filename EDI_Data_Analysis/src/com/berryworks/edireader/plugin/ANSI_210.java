/*
 * Copyright 2005-2011 by BerryWorks Software, LLC. All rights reserved.
 *
 * This file is part of EDIReader. You may obtain a license for its use directly from
 * BerryWorks Software, and you may also choose to use this software under the terms of the
 * GPL version 3. Other products in the EDIReader software suite are available only by licensing
 * with BerryWorks. Only those files bearing the GPL statement below are available under the GPL.
 *
 * EDIReader is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * EDIReader is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EDIReader.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

/*
*  Generated by plugin generator utility by BerryWorks Software, LLC.
*/
package com.berryworks.edireader.plugin;

import com.berryworks.edireader.Plugin;

public class ANSI_210 extends Plugin
{
  public ANSI_210()
  {
    super("210", "Motor Carrier Freight Details and Invoice");
    loops = new LoopDescriptor[]{
      new LoopDescriptor(".", "B3", 0, "*"),
      new LoopDescriptor(".", "C2", 0, "*"),
      new LoopDescriptor(".", "C3", 0, "*"),
      new LoopDescriptor(null, "G62", 1, "/S5"),
      new LoopDescriptor(".", "G62", 0, "*"),
      new LoopDescriptor(null, "H1", 1, "/LX"),
      new LoopDescriptor(null, "H2", 1, "/LX"),
      new LoopDescriptor(null, "H3", 1, "/S5"),
      new LoopDescriptor(".", "H3", 0, "*"),
      new LoopDescriptor(".", "ITD", 0, "*"),
      new LoopDescriptor(null, "K1", 1, "/LX"),
      new LoopDescriptor(".", "K1", 0, "*"),
      new LoopDescriptor(null, "L0", 1, "/LX"),
      new LoopDescriptor(null, "L1", 1, "/LX"),
      new LoopDescriptor(".", "L3", 0, "*"),
      new LoopDescriptor(null, "L4", 1, "/LX"),
      new LoopDescriptor(null, "L5", 1, "/LX"),
      new LoopDescriptor(null, "L7", 1, "/LX"),
      new LoopDescriptor("LX", "LX", 1, "*"),
      new LoopDescriptor(null, "M7", 3, "/S5/N1/N7"),
      new LoopDescriptor(null, "M7", 1, "/N7"),
      new LoopDescriptor("N1", "N1", 2, "/S5"),
      new LoopDescriptor("N1", "N1", 1, "*"),
      new LoopDescriptor(null, "N2", 2, "/S5/N1"),
      new LoopDescriptor(null, "N2", 1, "/N1"),
      new LoopDescriptor(null, "N3", 2, "/S5/N1"),
      new LoopDescriptor(null, "N3", 1, "/N1"),
      new LoopDescriptor(null, "N4", 2, "/S5/N1"),
      new LoopDescriptor(null, "N4", 1, "/N1"),
      new LoopDescriptor("N7", "N7", 3, "/S5/N1"),
      new LoopDescriptor("N7", "N7", 1, "*"),
      new LoopDescriptor(null, "N9", 2, "/S5/N1"),
      new LoopDescriptor(null, "N9", 1, "/N1"),
      new LoopDescriptor(".", "N9", 0, "*"),
      new LoopDescriptor(".", "R3", 0, "*"),
      new LoopDescriptor("S5", "S5", 1, "*"),
      new LoopDescriptor(".", "SE", 0, "*"),
      new LoopDescriptor(".", "ST", 0, "*"),
    };
  }
}
