package grondag.hard_science.matter;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Element
{
    private static Map<String, Element> all = new HashMap<String, Element>(); 
    
    public static final Element H = new Element("H", "Hydrogen", 1.0080);
    public static final Element He = new Element("He", "Helium", 4.0026);
    public static final Element Li = new Element("Li", "Lithium", 6.9400);
    public static final Element Be = new Element("Be", "Beryllium", 9.0122);
    public static final Element B = new Element("B", "Boron", 10.8100);
    public static final Element C = new Element("C", "Carbon", 12.0110);
    public static final Element N = new Element("N", "Nitrogen", 14.0070);
    public static final Element O = new Element("O", "Oxygen", 15.9990);
    public static final Element F = new Element("F", "Fluorine", 18.9980);
    public static final Element Ne = new Element("Ne", "Neon", 20.1800);
    public static final Element Na = new Element("Na", "Sodium", 22.9900);
    public static final Element Mg = new Element("Mg", "Magnesium", 24.3050);
    public static final Element Al = new Element("Al", "Aluminium", 26.9820);
    public static final Element Si = new Element("Si", "Silicon", 28.0850);
    public static final Element P = new Element("P", "Phosphorus", 30.9740);
    public static final Element S = new Element("S", "Sulfur", 32.0600);
    public static final Element Cl = new Element("Cl", "Chlorine", 35.4500);
    public static final Element Ar = new Element("Ar", "Argon", 39.9480);
    public static final Element K = new Element("K", "Potassium", 39.0980);
    public static final Element Ca = new Element("Ca", "Calcium", 40.0780);
    public static final Element Sc = new Element("Sc", "Scandium", 44.9560);
    public static final Element Ti = new Element("Ti", "Titanium", 47.8670);
    public static final Element V = new Element("V", "Vanadium", 50.9420);
    public static final Element Cr = new Element("Cr", "Chromium", 51.9960);
    public static final Element Mn = new Element("Mn", "Manganese", 54.9380);
    public static final Element Fe = new Element("Fe", "Iron", 55.8450);
    public static final Element Co = new Element("Co", "Cobalt", 58.9330);
    public static final Element Ni = new Element("Ni", "Nickel", 58.6930);
    public static final Element Cu = new Element("Cu", "Copper", 63.5460);
    public static final Element Zn = new Element("Zn", "Zinc", 65.3800);
    public static final Element Ga = new Element("Ga", "Gallium", 69.7230);
    public static final Element Ge = new Element("Ge", "Germanium", 72.6300);
    public static final Element As = new Element("As", "Arsenic", 74.9220);
    public static final Element Se = new Element("Se", "Selenium", 78.9710);
    public static final Element Br = new Element("Br", "Bromine", 79.9040);
    public static final Element Kr = new Element("Kr", "Krypton", 83.7980);
    public static final Element Rb = new Element("Rb", "Rubidium", 85.4680);
    public static final Element Sr = new Element("Sr", "Strontium", 87.6200);
    public static final Element Y = new Element("Y", "Yttrium", 88.9060);
    public static final Element Zr = new Element("Zr", "Zirconium", 91.2240);
    public static final Element Nb = new Element("Nb", "Niobium", 92.9060);
    public static final Element Mo = new Element("Mo", "Molybdenum", 95.9500);
    public static final Element Tc = new Element("Tc", "Technetium", 0);
    public static final Element Ru = new Element("Ru", "Ruthenium", 101.0700);
    public static final Element Rh = new Element("Rh", "Rhodium", 102.9100);
    public static final Element Pd = new Element("Pd", "Palladium", 106.4200);
    public static final Element Ag = new Element("Ag", "Silver", 107.8700);
    public static final Element Cd = new Element("Cd", "Cadmium", 112.4100);
    public static final Element In = new Element("In", "Indium", 114.8200);
    public static final Element Sn = new Element("Sn", "Tin", 118.7100);
    public static final Element Sb = new Element("Sb", "Antimony", 121.7600);
    public static final Element Te = new Element("Te", "Tellurium", 127.6000);
    public static final Element I = new Element("I", "Iodine", 126.9000);
    public static final Element Xe = new Element("Xe", "Xenon", 131.2900);
    public static final Element Cs = new Element("Cs", "Caesium", 132.9100);
    public static final Element Ba = new Element("Ba", "Barium", 137.3300);
    public static final Element La = new Element("La", "Lanthanum", 138.9100);
    public static final Element Ce = new Element("Ce", "Cerium", 140.1200);
    public static final Element Pr = new Element("Pr", "Praseodymium", 140.9100);
    public static final Element Nd = new Element("Nd", "Neodymium", 144.2400);
    public static final Element Pm = new Element("Pm", "Promethium", 0);
    public static final Element Sm = new Element("Sm", "Samarium", 150.3600);
    public static final Element Eu = new Element("Eu", "Europium", 151.9600);
    public static final Element Gd = new Element("Gd", "Gadolinium", 157.2500);
    public static final Element Tb = new Element("Tb", "Terbium", 158.9300);
    public static final Element Dy = new Element("Dy", "Dysprosium", 162.5000);
    public static final Element Ho = new Element("Ho", "Holmium", 164.9300);
    public static final Element Er = new Element("Er", "Erbium", 167.2600);
    public static final Element Tm = new Element("Tm", "Thulium", 168.9300);
    public static final Element Yb = new Element("Yb", "Ytterbium", 173.0500);
    public static final Element Lu = new Element("Lu", "Lutetium", 174.9700);
    public static final Element Hf = new Element("Hf", "Hafnium", 178.4900);
    public static final Element Ta = new Element("Ta", "Tantalum", 180.9500);
    public static final Element W = new Element("W", "Tungsten", 183.8400);
    public static final Element Re = new Element("Re", "Rhenium", 186.2100);
    public static final Element Os = new Element("Os", "Osmium", 190.2300);
    public static final Element Ir = new Element("Ir", "Iridium", 192.2200);
    public static final Element Pt = new Element("Pt", "Platinum", 195.0800);
    public static final Element Au = new Element("Au", "Gold", 196.9700);
    public static final Element Hg = new Element("Hg", "Mercury", 200.5900);
    public static final Element Tl = new Element("Tl", "Thallium", 204.3800);
    public static final Element Pb = new Element("Pb", "Lead", 207.2000);
    public static final Element Bi = new Element("Bi", "Bismuth", 208.9800);
    public static final Element Po = new Element("Po", "Polonium", 0);
    public static final Element At = new Element("At", "Astatine", 0);
    public static final Element Rn = new Element("Rn", "Radon", 0);
    public static final Element Fr = new Element("Fr", "Francium", 0);
    public static final Element Ra = new Element("Ra", "Radium", 0);
    public static final Element Ac = new Element("Ac", "Actinium", 0);
    public static final Element Th = new Element("Th", "Thorium", 232.0400);
    public static final Element Pa = new Element("Pa", "Protactinium", 231.0400);
    public static final Element U = new Element("U", "Uranium", 238.0300);
    public static final Element Np = new Element("Np", "Neptunium", 0);
    public static final Element Pu = new Element("Pu", "Plutonium", 0);
    public static final Element Am = new Element("Am", "Americium", 0);
    public static final Element Cm = new Element("Cm", "Curium", 0);
    public static final Element Bk = new Element("Bk", "Berkelium", 0);
    public static final Element Cf = new Element("Cf", "Californium", 0);
    public static final Element Es = new Element("Es", "Einsteinium", 0);
    public static final Element Fm = new Element("Fm", "Fermium", 0);
    public static final Element Md = new Element("Md", "Mendelevium", 0);
    public static final Element No = new Element("No", "Nobelium", 0);
    public static final Element Lr = new Element("Lr", "Lawrencium", 0);
    public static final Element Rf = new Element("Rf", "Rutherfordium", 0);
    public static final Element Db = new Element("Db", "Dubnium", 0);
    public static final Element Sg = new Element("Sg", "Seaborgium", 0);
    public static final Element Bh = new Element("Bh", "Bohrium", 0);
    public static final Element Hs = new Element("Hs", "Hassium", 0);
    public static final Element Mt = new Element("Mt", "Meitnerium", 0);
    public static final Element Ds = new Element("Ds", "Darmstadtium", 0);
    public static final Element Rg = new Element("Rg", "Roentgenium", 0);
    public static final Element Cn = new Element("Cn", "Copernicium", 0);
    public static final Element Nh = new Element("Nh", "Nihonium", 0);
    public static final Element Fl = new Element("Fl", "Flerovium", 0);
    public static final Element Mc = new Element("Mc", "Moscovium", 0);
    public static final Element Lv = new Element("Lv", "Livermorium", 0);
    public static final Element Ts = new Element("Ts", "Tennessine", 0);
    public static final Element Og = new Element("Og", "Oganesson", 0);
    
    static
    {
        all = ImmutableMap.copyOf(all);
    }
    
    public static Map<String, Element> all()
    {
        return all;
    }
    
    public final String symbol;
    public final String name;
    public final double weight;
    

    
    private Element(String symbol, String name, double weight)
    {
        this.symbol = symbol;
        this.name = name;
        this.weight = weight;
        all.put(symbol, this);
    }
}
