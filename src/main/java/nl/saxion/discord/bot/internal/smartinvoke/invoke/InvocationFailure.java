package nl.saxion.discord.bot.internal.smartinvoke.invoke;

/**
 * Represents diagnostics about the issue that occurred while creating the arguments for an invocation.
 * This currently carries no more than the probability in the invocation of the command.
 * This may be used to present the syntax of the command that the user probably tried to invoke
 */
final class InvocationFailure {
    /**
     * The probability in creating the arguments. This helps with identifying the command that the use probably meant to use
     */
    private final double probability;

    /**
     * Creates a new invocation failure
     * @param probability the probability in the transformation of arguments in the command
     */
    InvocationFailure(double probability) {
        this.probability = probability;
    }

    /**
     * @return the probability in the transformation of arguments in the command
     */
    public double getProbability() {
        return probability;
    }
}
