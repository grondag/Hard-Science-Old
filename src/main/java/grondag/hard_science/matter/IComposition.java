package grondag.hard_science.matter;

import com.google.common.collect.ImmutableList;

public interface IComposition
{

    double countOf(Element e);

    double weightOf(Element e);

    double weight();

    ImmutableList<Element> elements();
}