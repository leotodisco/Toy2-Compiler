package unisa.compilatori.semantic.visitor;

public interface Visitable {
    <T> T accept(Visitor<T> visitor) throws Exception;
}
