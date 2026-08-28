#import "@preview/boxed-sheet:0.1.2": *
#import "@preview/tdtr:0.5.4": tidy-tree-graph
#import "@preview/simple-plot:0.3.0": plot

#set text(font: (
  "Times New Roman",
  "SimSun",
))

#let homepage = link("https://github.com/timothyckl")[timothyckl.github.io]
#let author = "Timothy Chia"
#let title = "INF2008 – Finals Cheat Sheet"

#let my-colors = (
  rgb(190, 149, 196),
  rgb("#f39f71"),
  rgb(102, 155, 188),
  rgb(229, 152, 155),
  rgb("#6a4c93"),
  rgb("#E0A500"),
  rgb("#934c84"),
  rgb("#934c5a"),
)

#let H(p) = {
  if p <= 0.0 or p >= 1.0 {
    0.0
  } else {
    -p * calc.log(p, base: 2) - (1.0 - p) * calc.log(1.0 - p, base: 2)
  }
}

#show: boxedsheet.with(
  title: title,
  homepage: homepage,
  authors: author,
  write-title: true,
  title-align: left,
  title-number: true,
  title-delta: 2pt,
  scaling-size: false,
  font-size: 5.5pt,
  line-skip: 5.5pt,
  x-margin: 10pt,
  y-margin: 30pt,
  num-columns: 4,
  column-gutter: 2pt,
  numbered-units: false,
  color-box: my-colors,
)

= CRISP-DM Framework
#concept-block[
  *Definition*: The CRoss-Industry Standard Process for Data Mining (CRISP-DM) is a widely used framework that provides a structured lifecycle for planning and executing data mining and analytics projects. The methodology is highly iterative, meaning that moving back and forth between its different phases is common and expected.

  #inline("1. Business Understanding")
  This initial phase focuses on defining the project objectives, constraints, and success criteria from a business perspective rather than a technical one. It requires clearly identifying the existing problem, the decision to be improved, the evaluation metric, the ML intervention, and the quantifiable business value. In a production system, this translates into system configurations.

  #inline("2. Data Understanding")
  This phase involves data ingestion and exploratory analysis to understand the data's characteristics, limitations, and overall quality. In production systems, this maps to data collection components.

  #inline("3. Data Preparation")
  This step focuses on data cleaning, schema validation, and constructing feature pipelines. It translates to the data verification and feature extraction components of a production system.

  #inline("4. Modelling")
  This phase involves the actual algorithm development, logic training, and parameterisation. It represents the core ML code within the broader system framework.

  #inline("5. Evaluation")
  Before a model is approved for use, this phase involves offline evaluation, error analysis, and validation. In a production environment, this is handled by dedicated analysis tools.

  #inline("6. Deployment")
  The final phase involves rolling out the model for real-world application. In production, this requires vast surrounding infrastructure, including serving infrastructure, machine resource management, workflow orchestration, and post-deployment monitoring to observe system behaviour and track performance drift.
]

= Variable Types
#concept-block[
  #inline("Variable Roles")
  *Identifier*: These are unique identifiers for a row (like a Listing Number or Wafer ID) and should not be used as meaningful features in a model.

  *Feature (Independent)*: These are the inputs provided to a model that describe the situation or factors believed to influence an outcome, such as age or distance.

  *Target (Dependent) Variable*: This is the outcome or label that the model is trying to predict or explain, such as unit price or loan approval.

  #inline("Cardinal Data – Quantitative")
  These represent quantities where the values have magnitude, differences are meaningful, and arithmetic operations (like adding or averaging) make sense.

  *Discrete*: Consist of countable integers, such as the number of rooms or defect counts.

  *Continuous*: Can take any real value within a range, such as temperature, distance, or unit price.

  #inline("Categorical Data – Qualitative")
  These represent labels or groupings that partition data into classes, where arithmetic operations are not meaningful.

  *Ordinal*: Categories that have a meaningful sequence or order, though you cannot assume the mathematical spacing between them is equal (e.g. customer satisfaction ratings from "Poor" to "Excellent").

  *Nominal*: Categories that have no intrinsic order or ranking, making them interchangeable labels like colour, location, or lot number.

  *Other Types*: Variables can also take the form of text or booleans, and it is important to watch out for the "looks numeric" trap, where variables like postal codes appear as numbers but are actually categorical.

  #inline("Diagram")
  #align(center)[
    #tidy-tree-graph(
      spacing: (12pt, 8pt),
      node-inset: 1pt,
      compact: true,
    )[
      - Type
        - Quantitative
          - Discrete
          - Continuous
        - Qualitative
          - Ordinal
          - Nominal
    ]
  ]
]

= Data Quality Issues
#concept-block[
  #inline("Missing Data")
  Datasets often contain missing values (`NaN`), empty strings, or inconsistent entries. Unhandled missing values can cause silent bias, as many tools automatically drop these rows, skewing averages and comparisons.

  #inline("High Cardinality")
  Categorical variables with too many unique values (like lot numbers or postal codes) can cause one-hot encoding to explode the number of features.

  #inline("Improper Feature Use")
  Using unique row identifiers (like `wafer_id` or `Listing Number`) as features is a common trap, as they do not represent meaningful predictive attributes.

  #inline("\"Looks Numeric\" Trap")
  Variables like postal codes may appear as numbers but are actually categorical and should not be treated as mathematical quantities.
]

== Imputation Strategies
#concept-block[
  *Strategy A (Drop Rows)*: Dropping rows with missing values preserves semantic correctness but reduces the sample size and can introduce selection bias if the data is systematically missing.

  *Strategy B (Contextual Imputation)*: Filling missing values with contextual statistics (such as a mean, median, or mode) retains data rows but introduces artificial smoothing and weakens real relationships in the data.

  *Strategy C (Flagged Imputation)*: This is the preferred method. It fills missing values (like Strategy B) but also adds a binary flag column (for example, `1` if imputed and `0` if original). This preserves data, avoids silent bias, ensures transparency, and allows the model to learn if imputed rows behave differently.
]

== Sanity Checks
#concept-block[
  #inline("Plausibility")
  Verify that values are within realistic bounds, ensure minimums and maximums make sense, check for impossible combinations (for example, a 1-room flat with 3 bathrooms), and make sure units are consistent.

  #inline("Consistency")
  Check that categories are spelled consistently (for example, `AMK` vs `Ang Mo Kio`) and that group-level summaries do not contradict real-world expectations.
]

== Feature Leakage
#concept-block[
  Leakage occurs when a model is given information that would not naturally be available at the time of prediction, allowing it to "see the answer".

  This often happens when using post-outcome measurements or target-derived features. For example, using `resale_price_per_sqm` to predict total `resale_price`, or using a post-inspection defect count to predict an earlier-stage outcome.

  Leakage typically inflates training performance but fails badly in production.
]

== Class Imbalance and Representativeness
#concept-block[
  #inline("Representativeness")
  Check whether certain groups are under-represented, missing entirely, or whether the dataset fails to reflect the real-world population of interest. Missing groups create blind spots.

  #inline("Class Imbalance")
  This occurs when some categories are rare but important, such as churn or fraud. In such cases, raw accuracy can be misleading. A model can achieve very high accuracy while failing to detect the minority class at all.
]

== Granularity and Aggregation
#concept-block[
  #inline("Granularity")
  Ensure the data's level of detail matches the task. For example, predicting daily price changes is different from predicting yearly price changes. If you have multiple records for the same entity across time, aggregation may be necessary.

  #inline("Splitting / Structuring")
  If a single entry contains combined attributes (for example, `"MRT, School, Market"`), it should be split into distinct variables during preparation.
]

= Entropy
#concept-block[
  *Definition*: Entropy measures the lack of order or randomness in a dataset. A pure node has entropy `0`, while a perfectly balanced binary split has entropy `1`.

  #align(center)[
    $H = -sum_i p_i log_2 p_i$
  ]

  Where $p_i$ is the probability of samples belonging to the $i$th class.

  #inline("Evaluating a Split")
  A split is evaluated using the weighted entropy of the child nodes:

  #align(center)[
    $H_text("split") = (n_L / n) H(L) + (n_R / n) H(R)$
  ]

  Lower entropy means a purer split.

  #inline("Binary Entropy Curve")
  #align(center)[
    #image("entropy-plot.png")
  ]
]

= Gini Impurity
#concept-block[
  *Definition*: Gini impurity measures the probability that a randomly chosen data point in a node would be misclassified if you guessed its label using the class proportions of that node. A lower Gini value indicates a purer node, with `0` meaning all samples belong to a single class.

  #align(center)[
    $text("Gini") = 1 - sum_i p_i^2$
  ]

  Where $p_i$ is the proportion of the $i$th class in the node.

  Gini is often preferred in practice because it is faster to compute than entropy, since it avoids logarithms.

  #inline("Evaluating a Split")
  To evaluate whether a split is good, compare the impurity of the parent node with the weighted impurity of the child nodes:

  #align(center)[
    $G_text("split") = (n_L / n) G(L) + (n_R / n) G(R)$
  ]

  The best split is the one that most reduces impurity.
]

= Train-Test Split
#concept-block[
  *Train Set*: Used to fit candidate models. It is reused during development, but training accuracy should not be used to choose hyperparameters.

  *Test Set*: The final judge. It should be used exactly once at the very end and must not influence training or model selection.
  #image("train-test-split.png")

  #inline("Stratification")
  Stratification preserves the original class ratio in each split. This is especially important for classification tasks so that minority classes do not disappear from a split.

  #image("stratification.png")

  *Rule of thumb*: If the target is categorical, use `stratify = y`.

  #inline("Critical Rule")
  Fit all transformers (imputers, encoders, scalers) on the training set only, then apply `.transform()` to validation and test data. Never let validation or test data influence preprocessing fit parameters.

  #image("wrong-transform.png")
]

= Bias-Variance Trade-off
#concept-block[
  #inline("Error Decomposition")
  Expected prediction error can be viewed as:

  #align(center)[
    $text("Error") = text("Bias")^2 + text("Variance") + epsilon$
  ]
  
  *Bias* is error caused by overly strong assumptions. High-bias models are too simple, miss real patterns, and underfit. Both training and test error tend to be high.

  *Variance* is error caused by a model being too sensitive to the training data. High-variance models fit noise rather than signal, giving low training error but high test error.

  #image("error-decomp.png")
  
  #inline("Trade-off")
  Simpler models usually have higher bias and lower variance. More complex models usually have lower bias and higher variance. Good model selection aims for the lowest *total* error, not the lowest training error.

  #inline("Typical Scenarios")
  
  #image("image.png")
  
  *Underfitting*: High train error, high test error.

  *Overfitting*: Low train error, high test error.

  *Good Fit*: Low train error and similarly low test error.

  #inline("How to Control It")
  Cross-validation helps choose model complexity. For trees, regularisation such as `max_depth`, `min_samples_split`, and `min_samples_leaf` reduces variance. Bagging reduces variance, while boosting mainly reduces bias.
]

= Cross-Validation
#concept-block[
  #inline("K-Fold Cross-Validation")
  Split the training data into `k` folds. Train on `k - 1` folds and validate on the remaining fold. Repeat until every fold has served as validation exactly once, then average the performance across all runs.

  #inline("Algorithm")
  + Split training data into `k` equal folds.
  + Train on `k - 1` folds and validate on the remaining fold.
  + Repeat `k` times.
  + Average the validation metrics.
  + Select the model or hyperparameters with the best average CV score.
  + Retrain the final chosen model on the full training set.

  #image("cv.png")

  #inline("Key Principle")
  Cross-validation is used for model comparison and hyperparameter selection. The test set is still held back and used only once at the end. CV improves reliability, but it does not guarantee real-world performance.
]

= Confusion Matrix
#concept-block[
  #inline("Structure")
  
  A confusion matrix compares predicted labels against actual labels.

  + Actual positive, predicted positive = `TP`
  + Actual positive, predicted negative = `FN`
  + Actual negative, predicted positive = `FP`
  + Actual negative, predicted negative = `TN`

  #image("confusion-matrix.png")

  #inline("Error Types")
  *Type I Error*: False Positive (`FP`) — predicted positive, actually negative.

  *Type II Error*: False Negative (`FN`) — predicted negative, actually positive.

  #inline("Purpose")
  All major classification metrics are derived from `TP`, `FP`, `FN`, and `TN`. The confusion matrix is therefore the foundation for evaluating classification models.
]

= Classification Metrics
#concept-block[
  #inline("Core Formulas")

  #grid(
    columns: (1fr, 1.1fr), // Two columns with equal flexible space
    align: (left, right), // Align the first column left, the second right
    gutter: 0cm, // Adds space between columns
  
    // Content for the first column (left)
    [
      $text("Accuracy") = (text("TP") + text("TN")) / (text("TP") + text("TN") + text("FP") + text("FN"))$

      $text("Precision") = text("TP") / (text("TP") + text("FP"))$

      $text("Recall") = text("TP") / (text("TP") + text("FN"))$
    ],
  
    // Content for the second column (right)
    [
      #align(left)[
        $text("Specificity") = text("TN") / (text("TN") + text("FP"))$
      ]

      #align(left)[
        $text("Balanced Accuracy") = (text("Recall") + text("Specificity")) / 2$
      ]
      
      #align(left)[
        $F_1 = 2 dot (text("Precision") times text("Recall")) / (text("Precision") + text("Recall"))$
      ]
    ],
  )

  #inline("Intuition")
  *Accuracy*: Overall fraction classified correctly.

  *Precision*: Of all predicted positives, how many were actually positive?

  *Recall / Sensitivity*: Of all actual positives, how many were caught?

  *Specificity*: Of all actual negatives, how many were correctly rejected?

  *Balanced Accuracy*: Mean of recall and specificity; more robust when classes are imbalanced.

  *F1 Score*: Harmonic mean of precision and recall; useful when both false positives and false negatives matter.

  #inline("Metric Selection")
  *Accuracy*: Only reliable when classes are roughly balanced.

  *Recall*: Use when false negatives are costly, such as cancer screening or fraud detection.

  *Precision*: Use when false positives are costly, such as spam filtering.

  *F1*: Use when both false positives and false negatives matter.

  *Balanced Accuracy*: Prefer over raw accuracy when class imbalance is present.
]

= Decision Trees
#concept-block[
  #inline("General Idea")
  Decision trees recursively split the data so that child nodes become purer than the parent. They are easy to interpret but can overfit badly if allowed to grow without constraints.
  
  #inline("Terminology")
  *Root*: The single top-level node covering all samples.

  *Internal Node*: A decision node that splits on a feature threshold.

  *Leaf*: A terminal node that outputs a prediction.

  *Depth*: Number of levels from the root, where the root is depth `0`.

  *Decision Stump*: A depth-1 tree with one split and two leaves.

  #align(center)[
    #image("decision-tree.png")
  ]

  #inline("Stopping Criteria")
  *max_depth*: Maximum depth allowed from root to leaf.

  *min_samples_split*: Minimum number of samples required to attempt a split.

  *min_samples_leaf*: Minimum number of samples required in each leaf.

  Without stopping rules, trees can memorise the training data and achieve perfect training accuracy while generalising poorly.
]

== Classification Trees
#concept-block[
  *Purpose*: Classification trees predict discrete class labels. Each leaf usually outputs the majority class of the samples that fall into that leaf.

  #inline("How Splits Are Chosen")
  Candidate splits are evaluated using impurity measures such as Gini impurity or entropy. The best split is the one that produces the lowest weighted child impurity.

  #align(center)[
    $text("Impurity")_text("after") = n_L / n dot text("Impurity")(L) + n_R / n dot text("Impurity")(R)$
  ]

  #inline("Interpretation")
  A good split separates classes cleanly, producing purer child nodes than the parent. In practice, Gini and entropy usually choose very similar splits, and Gini is often the default because it is cheaper to compute.

  #inline("Risk")
  Deep trees have low bias but high variance. Pruning or limiting depth helps improve generalisation.
]

== Ensemble Methods
#concept-block[
  #inline("Definition")
  Ensemble learning combines multiple base learners and aggregates their predictions by voting (classification) or averaging (regression) to generalise better than a single model.

  #inline("When Ensembles Work")
  Ensembles work best when the individual models make diverse, uncorrelated errors.

  #inline("When They Fail")
  If all models make the same mistake, there is little or no benefit. If the data contains no useful signal, combining models cannot create one.

  #inline("Bagging")
  Train many models independently on bootstrapped datasets, then aggregate their predictions. Bagging mainly reduces variance.

  *Random Forests* are bagged decision trees with an extra source of randomness: at each split, only a random subset of features is considered. This decorrelates the trees.

  *Out-of-Bag (OOB) Samples*: For any one tree, about `36.8%` of training samples are not selected into its bootstrap sample. These OOB samples act like a built-in validation set.

  #inline("Boosting")
  Train models sequentially so that each new model focuses on the mistakes of earlier ones. Boosting mainly reduces bias, but can overfit if pushed too far.

  *AdaBoost*: Increases the weight of misclassified points so later learners focus on them more strongly.

  #inline("Gradient Boosting")
  Each new tree fits the residuals or loss gradient of the current ensemble.

  #align(center)[
    $text("Residual") = y - hat(y)$
  ]
  #align(center)[
    $text("Loss") = (y - hat(y))^2$
  ]
  #align(center)[
    $text("Gradient") = -2(y - hat(y))$
  ]

  A smaller learning rate makes each step more cautious, and early stopping is used when validation performance stops improving.

  #inline("Stacking")
  Stacking combines different model families using a meta-learner that learns when to trust each base model.
]

= Linear Models
#concept-block[
  #inline("Simple Linear Regression")
  *Simple Linear Regression* is given by:
  
  #align(center)[
    $hat(y)_i = w x_i plus b$
  ]

  Where $hat(y)_i$ is the predicted value for the $i$-th observation, $x_i$ is the input feature, and $w$ is the slope coefficient, and $b$  is the y-intercept.
  
  #inline("Multiple Linear Regression")
  *Multiple Linear Regression* is given by:
  
  #align(center)[
    $hat(y)_i = sum_(j=1)^p (w_j x_(i,j)) + b$
  ]

  Where $hat(y)_i$ is the predicted value for the $i$-th observation, $x_(i,1), x_(i,2), ..., x_(i,p)$ are the input features for that observation, $w_1, w_2, ..., w_p$ are the coefficients associated with each feature, and $b$ is the y-intercept.

  This also forms the basis of perceptrons, where this linear combination of inputs is followed by an activation function to produce the final output.
]

== Regression Metrics
#concept-block[
  #inline("MAE")
  *Mean Absolute Error* measures the average absolute prediction error:

  #align(center)[
    $text("MAE") = 1 / n sum_(i=1)^(n) |y_i - hat(y)_i|$
  ]

  It is interpretable in the same units as the target and is relatively robust to outliers.

  #inline("MSE")
  *Mean Squared Error* measures average squared prediction error:

  #align(center)[
    $text("MSE") = 1 / n sum_(i=1)^(n) (y_i - hat(y)_i)^2$
  ]

  It penalises large errors more heavily, but its units are squared.

  #inline("RMSE")
  *Root Mean Squared Error* is:

  #align(center)[
    $text("RMSE") = sqrt(text("MSE"))$
  ]

  It is in the same units as the target and is one of the most commonly reported regression metrics.

  #inline("HMSE")
  *Half Mean Squared Error* is:
  
  #align(center)[
    $text("HMSE") = 1/(2n) sum_(i=1)^(n)(y_i - hat(y)_i)^2$
  ]

  This is essentially $text("MSE")$ with the factor $1/2$ added at the front and is used for *mathematical convenience* during gradient descent. 
  
  This is because when differentiating the squared error term, the power rule introduces a factor 2. Thus, adding $1/2$ cancels this.

  Note: This *does not change* the location of its minimum.
  
  #inline("R Squared")
  $R^2$ measures the proportion of target variance explained by the model.

  + $R^2 = 1$: perfect prediction
  + $R^2 = 0$: no better than predicting the mean
  + $R^2 < 0$: worse than the mean predictor
]

= Linear Regression with Perceptron
#concept-block[
  With multiple linear regression as the basis for the perceptron, we can now apply it to different problem contexts. One use case of the perceptron is *Simple Linear Regression*.

  Recall that in *Multiple Linear Regression*, $hat(y)_i$ is given by:
  
  #align(center)[
    $hat(y)_i = sum_(j=1)^p (w_j x_(i,j)) + b$
  ]

  #image("perceptron.png")
  
  In the perceptron diagram above, each input feature is multiplied by its corresponding weight, and these weighted inputs are then added together with the bias term. This is the part labelled *summation and bias*, which represents:

  #align(center)[
    $sum_(j=1)^p (w_j x_(i,j)) + b$
  ]

  However, for *Simple Linear Regression*, there is only one input feature. Hence, the general perceptron expression simplifies to:

  #align(center)[
    $hat(y)_i = w x_i + b$
  ]

  So, in this case:
  - the multiple inputs in the diagram reduce to just one input $x_i$
  - the corresponding weight reduces to a single weight $w$
  - the summation block becomes $w x_i + b$

  For simple linear regression, the activation function is just the identity function, so it does not change the value from the summation block. Therefore, the final output is simply:

  #align(center)[
    $hat(y)_i = w x_i + b$
  ]

  The goal is to find the values of $w$ and $b$ that *minimise* the prediction error. This is given by:

  #align(center)[
    $min_(w,b) 1/(2n) sum_(i=1)^n (y_i - hat(y)_i)^2$
  ]

  #v(1em)
]

== Forward Pass
#concept-block[
  For each sample $i$, the model predicts:
  $
    hat(y)_i = w x_i + b
  $

  The residual for sample $i$ is:
  $
    e_i = y_i - hat(y)_i
  $

  If $e_i gt 0$, the model predicted too low.  \
  If $e_i lt 0$, the model predicted too high.
]

== Loss Computation
#concept-block[
  We do not sum residuals directly, because positive and negative errors can cancel out. Instead, we square the errors and use the $text("HMSE")$:
  $
    L(w, b) = 1/(2n) sum_(i=1)^n (y_i - hat(y)_i)^2
  $

  Since $hat(y)_i = w x_i + b$, the loss can also be written as:
  $
    L(w, b) = 1/(2n) sum_(i=1)^n (y_i - (w x_i + b))^2
  $

  The factor $1/2$ is included so differentiation during backpropogation becomes cleaner.

  Remember, the goal is $min_(w,b) L(w,b)$.

  
]

== Backpropogation
#concept-block[
  Backpropagation computes how the loss changes with respect to the parameters $w$ and $b$. Before taking partial derivatives, it helps to see the computation flow:
  $
    w, x_i, b -> z_i -> hat(y)_i -> L(w,b)
  $

  For simple linear regression:
  $z_i = w x_i + b$ and
  $hat(y)_i = z_i$.

  So the full flow is:
  $
    w, x_i, b -> (w x_i + b) -> hat(y)_i -> 1/(2n) sum_(i=1)^n (y_i - hat(y)_i)^2
  $

  This shows that $w$ and $b$ affect the prediction first, and the prediction then affects the loss. Using the chain rule, we trace that dependency backwards:

  #grid(
    columns: (1fr, 1fr), // Two columns with equal flexible space
    align: (center, center), // Align the first column left, the second right
    gutter: 0.1cm, // Adds space between columns
  
    // Content for the first column (left)
    [
      Loss wrt b
      $
      partial(L) / partial(b) = sum_(i=1)^n partial(L) / partial(z_i)dot partial(z_i)/ partial(b)
      $
    ],

    // Content for the second column (right)
    [
      Loss wrt w
      $
      partial(L) / partial(w) = sum_(i=1)^n partial(L) / partial(z_i) dot partial(z_i) / partial(w)
      $
    ],
  )

  Since $hat(y)_i = z_i$, the residual is $e_i = y_i - hat(y)_i$. Then the gradients become:

  #grid(
    columns: (1fr, 1fr), // Two columns with equal flexible space
    align: (center, center), // Align the first column left, the second right
    gutter: 0.1cm, // Adds space between columns
  
    // Content for the first column (left)
    [
      $
    partial(L) / partial(b) = -1/n sum_(i=1)^n (y_i - hat(y)_i) \ = -1/n sum_(i=1)^n e_i
  $
    ],

    // Content for the second column (right)
    [
      $
    partial(L) / partial(w) = -1/n sum_(i=1)^n (y_i - hat(y)_i) dot x_i \ = -1/n sum_(i=1)^n e_i x_i
  $
    ],
  )
]

== Gradient Descent
#concept-block[
  Gradient descent updates the parameters in the direction that reduces the loss.

  1. Choose a learning rate $alpha gt 0$
  2. Update learnable parameters ($w$, $b$)
    // $w := w - alpha (partial L / partial w)$
    // 

    #grid(
      columns: (1fr, 1fr), // Two columns with equal flexible space
      align: (center, center), // Align the first column left, the second right
      gutter: 0.1cm, // Adds space between columns
    
      // Content for the first column (left)
      [
        $
        w := w - alpha dot partial(L) / partial(w)
        $
      ],
  
      // Content for the second column (right)
      [
        $
        b := b - alpha dot partial(L) / partial(b)
        $
      ],
    )

  3. Repeat the forward pass, loss computation, and updating of parameters until convergence.
]

= Activation Functions
  #concept-block[
    An activation function $g(z)$ is applied to the linear computation 
    $
    z = w^T x + b
    $ at each layer of a neural network. The choice of activation function determines the network's ability to learn complex, non-linear mappings. 
  
    #inline("Motivation")
    If every layer uses a linear activation function 
    $
    g(z) = z,
    $ then the composition of layers collapses into a single linear transformation. For a two-layer network: 
    $
    a^([2]) = W^([2])(W^([1]) x + b^([1])) + b^([2]) \ = W' x + b'.
    $ 
    *Stacking linear layers is equivalent to a single linear layer*, so depth adds no expressive power. Non-linear activations prevent this collapse, enabling the network to learn complex functions. 
    
    Crucially, they also allow hidden units to capture _feature interactions_ — combinations of inputs that no single linear term can represent — which is what lets deeper layers build progressively richer representations.

    #v(1em)
  ]
  

== Inner Activations
#concept-block[
  #grid(
    columns: (1fr, 1fr, 1fr), // Two columns with equal flexible space
    align: (center, center, center), // Align the first column left, the second right
    gutter: 0.1cm, // Adds space between columns
    // Content for the first column (left)
    [
      
      $tanh(x) = (e^x minus e^(-x))/(e^x plus e^(-x))$
    ],
  
    // Content for the second column (middle)
    [
      
      $text("relu")(z) = max(0, z)$
    ],

    // Content for the third column (right)
    [
      
      $text("lrelu")(z) = max(alpha z, z)$
    ],
  )

  #image("inner-activation.png")
]


== Outer Activations
#concept-block[
  #grid(
      columns: (1fr, 1fr), // Two columns with equal flexible space
      align: (center, center, center), // Align the first column left, the second right
      gutter: 0.1cm, // Adds space between columns
    
      // Content for the first column (left)
      [
        $text("sigmoid")(z) = 1/(1 plus e^(minus x))$
      ],
    
      // Content for the second column (right)
      [
        $f(z) = z$
      ],
    )
    
    #image("outer-activation.png")

  #inline("Softmax")
  For a vector $z = [z_1, z_2, ..., z_K]$:
  $ 
  "softmax"(z_i) = e^(z_i) / (sum_(j=1)^K e^(z_j)) 
  $
  
  *Softmax is a generalisation of sigmoid* to multiple classes. It takes $K$ raw scores (logits) which can be positive, negative, or zero — and transforms them into $K$ values between 0 and 1 that sum to 1, *forming a valid probability distribution*. 
  
  Large inputs become large probabilities; small or negative inputs become small probabilities. It can only be used when classes are mutually exclusive.
]


  
= Distance Metrics
#concept-block[
  #inline("Euclidean Distance")
  // #image("eulid.png")
  *Euclidean distance* is given by:
  $
  d(x, y) = sqrt(sum_(i=1)^(p) (x_i minus y_i)^2) = sqrt(sum (Delta x)^2 + (Delta y)^2)
  $

  Where $Delta x = x_2 minus x_1$ and $Delta y = y_2 minus y_1$ on a two-dimensional plane.
  
  #inline("Manhattan Distance")
  *Mahattan distance* is given by:
  $
  d(x, y) = sum_(i=1)^(p) abs(abs((x_i minus y_i))) = abs(Delta x) plus abs(Delta y)
  $
  
  #inline("Chebyshev Distance")
  *Chebyshev distance* is given by:
  $
  d(x, y) = max_(1 gt.eq i gt.eq p) abs(abs((x_i minus y_i))) = max(abs(Delta x), abs(Delta y))
  $

  #v(1em)
]

= Normalisation
#concept-block[
  Normalisation is important to avoid the "scaling trap" caused by features of differing scales.

  Without normalisation, the feature with the larger numeric range dominates distance-based metrics, which can lead to incorrect neighbours and wrong classification.
  
  To normalise a feature $x$ to the range of $[0,1]$, we use the following equation:

  $
  z_i = (x_i minus min(x))/(max(x) minus min(x))
  $ 
  
]

= Cluster Analysis Metrics
#concept-block[
  Metrics used in cluster analysis can help to tell us how well-grouped a clusters are and how distinct clusters are from one another.

  
  #inline("Inertia Score")
  *Inertia* or *Within Cluster Sum of Squares (WCSS)* measures the sum of squared distances between each point and its cluster centroid, summed across all clusters. It is given by:

  $
  J = sum_(j=1)^(K) sum_(x_i in C_j) abs(abs(x_i - mu_i))^2
  $

  Where:
  - $J$ is loss function to minimise
  - $K$ is the number of clusters
  - $j$ is the cluster index from 1 to $k$
  - $x_i$ is the $i^"th"$ data point in the dataset
  - $x_i in C_j$ is the $i^"th"$ data point that belongs to cluster $C_j$
  - $mu_j$ is the centroid (mean vector) or cluster $C_j$
  
  The mean vector $mu_j$ is given by:
  
  $
  mu_j = 1/abs(C_j) dot sum_(x_i in C_j) x_i
  $
  
  It is considered a *global* metric, because it tells us about the *overall compactness* of all clusters, but not how *separable* the clusters are.

  #inline("Silhouette Score")
  The *Silhouette* score is composed of two metrics: 
  
  + *Cohesion*, denoted $a(i)$, measures the mean distance to *all other points in the same cluster.*
  + *Separation*, denoted $b(i)$, measures the mean distance to *all points in the nearest neighbouring cluster.*

  Combining cohesion and separation gives us the silhouette score which is given by:

  $
  s(i) = (b(i) minus a(i))/max(a(i), b(i))
  $

  
  The *average silhouette score* is given by:
  $
   overline(s) = 1/n dot sum_(i=1)^(n) s(i)
  $

  Where $forall i in {1, dots, n}$, $s_i$ is the silhouette score or point $x_i$.

  #inline("Silhouette Score Interpretation")
  #grid(
    columns: (1fr, 1fr, 1fr), // Two columns with equal flexible space
    align: (center, center, center), // Align the first column left, the second right
    gutter: 0cm, // Adds space between columns
  
    [
      $s(i) approx plus 1$\
      Well clustered
    ],
    [
      $s(i) approx 0$\
      On boundary
    ],
    [
      $s(i) lt 0$\
      Likely misplaced
    ],
  )

  #inline("Distance as a Loss Function")

  Within each cluster $C_k$, we want to find a centroid $mu_k$ such that the variance between points within $C_k$ and $mu_k$ is minimised. This is given by:

  $
  min_(mu_k) sum_(x_i in C_k) abs(abs(x_i minus mu_k))^2
  $
]

= Clustering
#concept-block[
  A subfield of machine learning is *Unsupervised Learning*. Unlike in Supervised Learning, we instead deal with *unlabeled data* and attempt to learn partitions or groups from the unlabeled datasets.

  #inline("Example")

  1. We are given only dataset of independent variables $X = {x_1, x_2, x_3, ..., x_n}$. 
  
  2. There are no target variables or labels.

  3. The goal is to learn hidden structures and group similar data points.
]

== K-Means
#concept-block[
  Partition-based clustering method that groups points by assigning them to the nearest centroid and updating centroids iteratively.

  #inline("Steps")

  + Choose the number of clusters $k$. 
  + Randomly assign $k$ initial centroids.
  + Measure the squared distance between a point to each cluster's centroid.
  + Assign the data point to its nearest centroid's cluster.
  + Repeat steps 3-4 for all data points.
  + Recompute each cluster's centroid to find $mu^(*)_(k)$.
  + Repeat steps 3-6 until convergence

  #inline("Convergence")
  
  + Stop when the maximum number of iterations is reached.
  + Stop when the change in loss falls below a threshold.
  + Stop when centroid movement falls below a threshold.

  #inline("Limitations")

  - *Must choose the number of clusters `k` beforehand*

  - *Choosing `k` is fundamentally heuristic*

  - *The elbow method is subjective and may not show a clear elbow*

  - *Loss always decreases as `k` increases, so larger `k` can look artificially better*

  - *Sensitive to the initial choice of centroids*

  - *Sensitive to outliers*

  - *Works best when clusters are roughly compact, spherical, and similarly sized*

  - *Can struggle in high dimensions because cluster structure becomes less distinct*

  - *In high dimensions, elbow curves often become smoother and harder to interpret*

  - *Dimensionality reduction such as PCA is often applied before clustering*
]


== K-Means++
#concept-block[
  Improved initialisation method for K-Means that chooses starting centroids more carefully to spread them out before clustering begins.

  #inline("How it works")

  + Choose the first centroid randomly.
  + For each remaining point, compute its distance $D(x_i)$ to the nearest already chosen centroid.
  + Choose the next centroid with probability proportional to the squared distance:
    $
    P(x_i) ∝ D(x_i)^2
    $
  + Equivalently,
    $
    P(x_i) = D(x_i)^2 / sum_j(D(x_j)^2)
    $
  + Repeat until $k$ initial centroids have been selected.
  + Run standard K-Means using these initial centroids.

  #inline("Intuition")

  + Points far from existing centroids are more likely to be selected.
  + This spreads the initial centroids across the dataset.
  + It reduces the chance that multiple centroids start in the same dense region.

  #inline("Benefits")

  + Usually gives better starting centroids than fully random initialisation.
  + Often converges faster than standard K-Means.
  + Can improve clustering quality by avoiding poor initial placements.
  + Helps separate well-spaced groups early in the algorithm.

  #inline("Limitations")

  *Still requires choosing the number of clusters $k$ beforehand*

  *Still inherits the main weaknesses of K-Means after initialisation*

  *Can still perform poorly when clusters are non-spherical, overlapping, or have very different sizes*

  *Sensitive to outliers, since faraway points can get high selection probability*

  *Improves centroid initialisation, but does not guarantee the globally optimal clustering*

  #inline("Use Cases")

  + When standard K-Means gives unstable results due to random initialisation.
  + When you want a better default initialisation for centroid-based clustering.
  + When clusters are expected to be reasonably compact and separated.
  + Commonly used as the standard practical version of K-Means.
]


// == K-Means++
// #concept-block[
//   #inline("")

//   #inline("Limitations")

//   #inline("Use Cases")
// ]

== DBSCAN
#concept-block[
  Density-based clustering method that finds dense regions and marks sparse points as outliers.

  + *Core Point:* a point with at least `min_samples` neighbours within radius `epsilon`
  + *Border Point:* a point within `epsilon` of a core point, but without enough neighbours to be a core point itself
  + *Noise Point:* a point that does not belong to any dense region and is marked as an outlier

  #inline("Hyperparameters")
  
  + `epsilon`: the radius within which points are considered neighbours
  + `min_samples`: the minimum number of points required to form a dense region (cluster)
  
  #inline("Limitations")

  - Choosing the right `epsilon` and `min_samples` can be tricky

  - Struggles when cluster densities differ significantly

  - Large `epsilon` can over-merge clusters

  - Too small `epsilon` can cause fragmentation

  - Higher `min_samples` makes DBSCAN more conservative and marks more points as noise

  - Lower `min_samples` can create too many small clusters
]

== K-Means vs. DBSCAN
#concept-block[
  #table(
  columns: (auto, 0.5fr, 0.5fr),
  inset: 10pt,
  align: horizon,
  table.header(
    [*Feature*], [*K-Means*], [*DBSCAN*],
  ),

  [Requires $k$?], [Yes], [No],
  [Shape Assumption], [Spherical / Convex], [Arbitrary],
  [Handles Noise], [No], [Yes],
  [Sensitive to Scaling], [Yes], [Yes],
  [Sensitive to Initialization], [Yes], [No],
  [Struggles With], [Irregular shapes], [Varying densities],
  [Optimization based], [Yes (minimizes J)], [No],
  [Produces convex clusters], [Yes], [No],
  [Works Well When], [Compact clusters], [Density-separated clusters],
)
]
